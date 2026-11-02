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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.UUID

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
    // UI States
    val scope = rememberCoroutineScope()
    var isManual by remember { mutableStateOf(false) }

    // Search States
    var selectedFood by remember { mutableStateOf<UsdaFood?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UsdaFood>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Manual Inputs
    var quantity by remember { mutableStateOf("1") }
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text(if (isManual) "Add Custom Food" else if (selectedFood != null) "How many servings?" else "Search Food")},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Mode
                if (!isManual && selectedFood == null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Food name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Search Button
                    Button(
                        onClick = {
                            isSearching = true
                            errorMessage = ""
                            scope.launch {
                                try {
                                    val response = RetrofitInstance.api.searchFoods(searchQuery)
                                    val rawList = response.foods.filter { it.description.isNotBlank() && it.foodNutrients.isNotEmpty() }
                                    val distinctList = rawList.distinctBy { it.description.lowercase() }
                                    searchResults = distinctList.sortedWith(
                                        compareByDescending<UsdaFood> { it.description.contains(searchQuery, ignoreCase = true) }
                                            .thenBy { it.description.length }
                                    )
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                    //errorMessage = "Failed to search. Check internet."
                                } finally {
                                    isSearching = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        !isSearching
                    ) {
                        Text(if (isSearching) "Searching for food..." else "Search Database")
                    }
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(searchResults) { food ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFood = food }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(food.description, style = MaterialTheme.typography.bodyLarge)
                                val categoryLabel = food.foodCategory ?: "Unknown Category"
                                val subtitleText = if (!food.brandOwner.isNullOrBlank()) {
                                    "Brand: ${food.brandOwner} • $categoryLabel"
                                } else {
                                    "Category: $categoryLabel"
                                }
                                Text(
                                    text = subtitleText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("${food.calories.toInt()} kcal per 100g", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider()
                        }
                    }
                    TextButton(
                        onClick = { isManual = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Can't find your food? Add it manually")
                    }
                // Determine Quantity
                } else if (selectedFood != null) {
                    Text("Selected: ${selectedFood!!.description}", style = MaterialTheme.typography.titleMedium)
                    val baseCal = selectedFood!!.calories

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (e.g., 2.5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val q = quantity.toDoubleOrNull() ?: 0.0
                    Text("Total Calories: ${(baseCal * q).toInt()} kcal")
                    // Manual Mode
                } else if (isManual) {
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Pro (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = carbs,
                            onValueChange = { carbs = it },
                            label = { Text("Carb (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fat,
                            onValueChange = { fat = it },
                            label = { Text("Fat (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val timestamp = System.currentTimeMillis()
                        val id = UUID.randomUUID().toString()

                        if (selectedFood != null) {
                            val quan = quantity.toDoubleOrNull() ?: 1.0
                            val meal = Meal(
                                id = id,
                                name = selectedFood!!.description,
                                calories = (selectedFood!!.calories * quan).toInt(),
                                protein = (selectedFood!!.protein * quan).toInt(),
                                carbs = (selectedFood!!.carbs * quan).toInt(),
                                fat = (selectedFood!!.fat * quan).toInt(),
                                timestamp = timestamp
                            )
                            onSave(meal)
                        } else if (isManual) {
                            val meal = Meal(
                                id = id,
                                name = foodName.ifBlank { "Custom Food" },
                                calories = calories.toIntOrNull() ?: 0,
                                protein = protein.toIntOrNull() ?: 0,
                                carbs = carbs.toIntOrNull() ?: 0,
                                fat = fat.toIntOrNull() ?: 0,
                                timestamp = timestamp
                            )
                            onSave(meal)
                        }
                    },
                    enabled = (selectedFood != null && quantity.toDoubleOrNull() != null) || (isManual && foodName.isNotBlank() && calories.isNotBlank())
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (selectedFood != null) selectedFood = null
                        else if (isManual) isManual = false
                        else onDismiss()
                    }
                ) {
                    Text(if (selectedFood != null || isManual) "Back" else "Cancel")
                }
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