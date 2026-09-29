package com.example.lock_in.food

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lock_in.data.Prf

@Composable
fun Nut() {
    val c = LocalContext.current
    val u = remember{ Prf.ld(c) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nutrition", style = MaterialTheme.typography.headlineSmall)
        if (u == null) { Text("Finish onboarding to get your targets."); return@Column }
        Card(Modifier.fillMaxWidth()) {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Today", style = MaterialTheme.typography.titleMedium)
            Bar("Calories", 0, u.kcal, "kcal")
            Bar("Protein", 0, u.pro, "g"); Bar("Carbs", 0, u.carb, "g"); Bar("Fat", 0, u.fat,"g")
          }
        }
        Text("No meals logged yet.")
    }
}

@Composable
private fun Bar(nm: String, cur: Int, max: Int, un: String) = Column {
    Text("$nm  $cur / $max $un")
    LinearProgressIndicator({ if (max > 0) cur.toFloat()/max else 0f }, Modifier.fillMaxWidth())
}
