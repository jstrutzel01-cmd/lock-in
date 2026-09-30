package com.example.lock_in.food

import androidx.compose.foundation.layout.*
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

@Composable
fun Nut() {
    val c = LocalContext.current
    val u = remember { Prf.ld(c) }

    var showDialog by remember { mutableStateOf(false) }

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
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today", style = MaterialTheme.typography.titleMedium)
                    Bar("Calories", 0, u.kcal, "kcal")
                    Bar("Protein", 0, u.pro, "g"); Bar("Carbs", 0, u.carb, "g"); Bar(
                    "Fat",
                    0,
                    u.fat,
                    "g"
                )
                }
            }
            Text("No meals logged yet.")
        }
    }

    if (showDialog) {
        AddFoodDialog(
            onDismiss = { showDialog = false },
            onSave = { foodName, calories ->
                showDialog = false
            }
        )
    }
}
@Composable
private fun Bar(nm: String, cur: Int, max: Int, un: String) = Column {
    Text("$nm $cur / $max $un")
    LinearProgressIndicator(
        progress = if (max > 0) cur.toFloat() / max else 0f,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AddFoodDialog(onDismiss: () -> Unit, onSave: (String, Int) -> Unit){
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Log Meal")},
        text = {
            Column{
                OutlinedTextField(
                    value = foodName,
                    onValueChange = {foodName = it},
                    label = {Text("Food Name")}
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = {calories = it},
                    label = {Text("Calories")}
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(foodName, calories.toIntOrNull() ?: 0)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel")}
        }
    )
}

