package com.example.lock_in.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

data class Meal (
    @DocumentId val id: String = "",
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
@Composable
fun Nut(nutViewModel: NutViewModel = viewModel()) {
    // Constants
    val c = LocalContext.current
    val u = remember { Prf.ld(c) }
    val meals by nutViewModel.meals.collectAsState()
    //Variables
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit){
        nutViewModel.fetchMealsFromFirebase()
    }

    // "Add" Button
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
                Text("Finish onboarding to get your targets."); return@Column
            } // "Today's Nutrition" Card
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Today", style = MaterialTheme.typography.titleMedium)
                    val totalCalories = meals.sumOf { it.calories }
                    val totalProtein = meals.sumOf { it.protein }
                    val totalCarbs = meals.sumOf { it.carbs }
                    val totalFat = meals.sumOf { it.fat }
                    Bar(nm = "Calories", cur = totalCalories, max = u.kcal, un = "kcal")
                    Bar(nm = "Protein", cur = totalProtein, max = u.pro, un = "g")
                    Bar(nm = "Carbs", cur = totalCarbs, max = u.carb, un = "g")
                    Bar(nm = "Fat", cur = totalFat, max = u.fat, un = "g")
                }
            }
            if (meals.isEmpty()) {
                Text("No meals logged yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val groupedMeals = meals.groupBy { formatDay(it.timestamp) }
                    groupedMeals.forEach { (dayString, mealsForDay) ->
                        // List of Days
                        item {
                            Text(
                                text = "$dayString:",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        // List of meal cards
                        items(mealsForDay, key = { it.id }) { meal ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { state ->
                                    if (state == SwipeToDismissBoxValue.EndToStart) {
                                        nutViewModel.deleteMeal(meal.id)
                                        true
                                    } else {
                                        false
                                    }
                                },
                            )
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = "${formatTime(meal.timestamp)}:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                                // Delete functionality
                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .clip(CardDefaults.shape)
                                                .background(Color.Red)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Meal",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    // Card used by individual meals
                                    content = {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    meal.name,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    "${meal.calories} kcal",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // "Add Food" Popup
    if (showDialog) {
        AddFoodDialog(
            onDismiss = { showDialog = false },
            onSave = { newMeal ->
                nutViewModel.saveMealToFirebase(newMeal)
                showDialog = false }
        )
    }
}

//Function - Draws progress bars
@Composable
private fun Bar(nm: String, cur: Int, max: Int, un: String) = Column {
    Text("$nm $cur / $max $un")
    LinearProgressIndicator(
        progress = if (max > 0) cur.toFloat() / max else 0f,
        modifier = Modifier.fillMaxWidth()
    )
}

// Function - Saves users input from "add food" popup
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

