package com.example.lock_in.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lock_in.data.Prf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.DocumentId
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.material3.CardDefaults
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput

data class Meal (
    @DocumentId val id: String = "",
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

// Main
@Composable
fun Nut(nutViewModel: NutViewModel = viewModel()) {
    val c = LocalContext.current
    val u = remember { Prf.ld(c) }
    val meals by nutViewModel.meals.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var mealToEdit by remember { mutableStateOf<Meal?>(null) }

    LaunchedEffect(Unit){
        nutViewModel.fetchMealsFromFirebase()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Log Food")
            }
        }
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nutrition", style = MaterialTheme.typography.headlineSmall)
            if (u == null) {
                Text("Finish onboarding to get your targets.")
                return@Column
            }

            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Today", style = MaterialTheme.typography.titleMedium)

                    val todaysMeals = meals.filter { formatDay(it.timestamp) == "Today" }
                    val totalCalories = todaysMeals.sumOf { it.calories }
                    val totalProtein = todaysMeals.sumOf { it.protein }
                    val totalCarbs = todaysMeals.sumOf { it.carbs }
                    val totalFat = todaysMeals.sumOf { it.fat }

                    Bar(nm = "Calories", cur = totalCalories, max = u.kcal, un = "kcal")
                    Bar(nm = "Protein", cur = totalProtein, max = u.pro, un = "g")
                    Bar(nm = "Carbs", cur = totalCarbs, max = u.carb, un = "g")
                    Bar(nm = "Fat", cur = totalFat, max = u.fat, un = "g")
                }
            }
            if (meals.isEmpty()) {
                Text("No meals logged yet.")
            } else {
                val groupedMeals = meals.groupBy { formatDay(it.timestamp) }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedMeals.forEach { (dayString, mealsForDay) ->
                        item {
                            Text(
                                text = "$dayString:",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        val mealsByTime = mealsForDay.groupBy { formatTime(it.timestamp) }
                        mealsByTime.forEach { (timeString, mealsAtSameTime) ->
                            item {
                                Text(
                                    text = "$timeString:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        start = 4.dp,
                                        bottom = 8.dp,
                                        top = 8.dp
                                    )
                                )
                            }
                            items(mealsAtSameTime, key = { it.id }) { meal ->
                                val twentyFourHoursAgo = System.currentTimeMillis() - (24L * 60 * 60 * 1000)
                                val isWithin24Hours = meal.timestamp >= twentyFourHoursAgo

                                if (isWithin24Hours) {
                                    val scope = rememberCoroutineScope()
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { state ->
                                            when (state) {
                                                SwipeToDismissBoxValue.StartToEnd -> {
                                                    nutViewModel.deleteMeal(meal.id)
                                                    true
                                                }
                                                SwipeToDismissBoxValue.EndToStart -> {
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(150)
                                                        mealToEdit = meal
                                                    }
                                                    false
                                                }
                                                else -> false
                                            }
                                        },
                                        positionalThreshold = { totalDistance -> totalDistance * 0.3f }
                                    )
                                    // Deletion and Modification Visuals
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        backgroundContent = {
                                            val direction = dismissState.dismissDirection
                                            val backgroundColor = when (direction) {
                                                SwipeToDismissBoxValue.StartToEnd -> Color.Red
                                                SwipeToDismissBoxValue.EndToStart -> Color.Blue
                                                else -> Color.Transparent
                                            }
                                            val icon = when (direction) {
                                                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Edit
                                                else -> Icons.Default.Delete
                                            }
                                            val alignment = when (direction) {
                                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                                else -> Alignment.Center
                                            }

                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .clip(CardDefaults.shape)
                                                    .background(backgroundColor)
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = alignment
                                            ) {
                                                if (direction != SwipeToDismissBoxValue.Settled) {
                                                    Icon(icon, contentDescription = null, tint = Color.White)
                                                }
                                            }
                                        },
                                        // Generates recent meals
                                        content = {
                                            MealCardItem(meal = meal)
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                            .pointerInput(Unit) {
                                                detectHorizontalDragGestures(
                                                    onDragStart = {
                                                        android.widget.Toast.makeText(
                                                            c,
                                                            "You cannot modify food older than 24 hours.",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    onHorizontalDrag = { change, _ -> change.consume() }
                                                )
                                            }
                                    ) {
                                        // Generates meals older than 24hrs
                                        MealCardItem(meal = meal)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddFoodDialog(
                onDismiss = { showDialog = false },
                onSave = { newMeal ->
                    nutViewModel.saveMealToFirebase(newMeal)
                    showDialog = false
                }
            )
        }

        mealToEdit?.let { meal ->
            ModifyFoodDialog(
                meal = meal,
                onDismiss = { mealToEdit = null },
                onSave = { updatedMeal ->
                    nutViewModel.saveMealToFirebase(updatedMeal)
                    mealToEdit = null
                }
            )
        }
    }
}

// Draws progress bars
@Composable
private fun Bar(nm: String, cur: Int, max: Int, un: String) = Column {
    Text("$nm $cur / $max $un")
    LinearProgressIndicator(
        progress = if (max > 0) cur.toFloat() / max else 0f,
        modifier = Modifier.fillMaxWidth()
    )
}

// Saves users input from "add food" popup
@Composable
fun AddFoodDialog(onDismiss: () -> Unit, onSave: (Meal) -> Unit){
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Log Meal")},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") })
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") })
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") })
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") })
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat (g)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val newMeal = Meal(
                    name = foodName,
                    calories = calories.toIntOrNull() ?: 0,
                    protein = protein.toIntOrNull() ?: 0,
                    carbs = carbs.toIntOrNull() ?: 0,
                    fat = fat.toIntOrNull() ?: 0
                )
                onSave(newMeal)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel")}
        }
    )
}
// Saves users input from "modify food" popup
@Composable
fun ModifyFoodDialog(meal: Meal, onDismiss: () -> Unit, onSave: (Meal) -> Unit) {
    var foodName by remember { mutableStateOf(meal.name) }
    var calories by remember { mutableStateOf(if (meal.calories > 0) meal.calories.toString() else "") }
    var protein by remember { mutableStateOf(if (meal.protein > 0) meal.protein.toString() else "") }
    var carbs by remember { mutableStateOf(if (meal.carbs > 0) meal.carbs.toString() else "") }
    var fat by remember { mutableStateOf(if (meal.fat > 0) meal.fat.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modify meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") })
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") })
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") })
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") })
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat (g)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                // Uses .copy() to retain the same Firestore Document ID and Timestamp
                val updatedMeal = meal.copy(
                    name = foodName,
                    calories = calories.toIntOrNull() ?: 0,
                    protein = protein.toIntOrNull() ?: 0,
                    carbs = carbs.toIntOrNull() ?: 0,
                    fat = fat.toIntOrNull() ?: 0
                )
                onSave(updatedMeal)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Formats date for listing
fun formatDay(timestamp: Long): String{
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
    val currentYear = calendar.get(Calendar.YEAR)
    calendar.timeInMillis = timestamp
    val mealDay = calendar.get(Calendar.DAY_OF_YEAR)
    val mealYear = calendar.get(Calendar.YEAR)

    return when{
        currentYear == mealYear && currentDay == mealDay -> "Today"
        currentYear == mealYear && currentDay - mealDay == 1 -> "Yesterday"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

// Formats time for listing
fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

// Generates Meal Card
@Composable
fun MealCardItem(meal: Meal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(meal.name, style = MaterialTheme.typography.bodyLarge)
            Text("${meal.calories} kcal", style = MaterialTheme.typography.bodyMedium)
        }
    }
}