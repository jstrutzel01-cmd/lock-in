package com.example.lock_in.lock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun Lck(tst: () -> Unit) {
    val c = LocalContext.current
    val ok = Usg.has(c)&&Usg.ovl(c)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Screen Time", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Earned time", style = MaterialTheme.typography.titleMedium)
                Text("0 min", style = MaterialTheme.typography.headlineMedium)
            }
        }
        Text(if (ok) "Permissions ready" else "Lock-In still needs permissions to block apps", color = if(ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)

        Text("Blocked apps", style=MaterialTheme.typography.titleMedium)
        Text("No apps picked yet.")
        OutlinedButton(tst, Modifier.fillMaxWidth()) { Text("Screen time API test") }
    }
}
