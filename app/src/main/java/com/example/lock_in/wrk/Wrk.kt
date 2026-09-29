package com.example.lock_in.wrk

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val GRPS = listOf("Chest","Back","Legs","Shoulders","Arms","Full body")

@Composable
fun Wrk(){
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("Workouts", style = MaterialTheme.typography.headlineSmall)
    Text("Preset routines", style = MaterialTheme.typography.titleMedium)
    GRPS.forEach { Card(Modifier.fillMaxWidth()) { Text(it, Modifier.padding(16.dp)) } }
    Text("My routines", style = MaterialTheme.typography.titleMedium)
    Text("You haven't made any routines yet.")
  }
}
