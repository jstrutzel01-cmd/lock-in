package com.example.lock_in.lock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UsgT(bk: () -> Unit) {
    val c = LocalContext.current
    var ua by remember { mutableStateOf(false) }
    var ov by remember { mutableStateOf(false) }
    var rc by remember { mutableStateOf(listOf<Pair<String, Long>>()) }
    var td by remember { mutableStateOf(listOf<Pair<String, Long>>()) }
    val tf = remember { SimpleDateFormat("h:mm:ss a", Locale.US) }

    LaunchedEffect(Unit) {
        while (true) {
            ua = Usg.has(c); ov = Usg.ovl(c)
            if (ua) {
                rc = Usg.rec(c)
                td = Usg.tdy(c)
            }
            delay(2000)
        }
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            Column {
                TextButton(bk) { Text("< Back") }
                Text("Screen time API test", style = MaterialTheme.typography.headlineSmall)
                Text("open another app for a few sec then come back, it should show up under recent apps", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
        }
        item { Prm("Usage access", ua) { Usg.gUsg(c) } }
        item {Prm("Display over other apps", ov) { Usg.gOvl(c) }}

        item { Column {
            HorizontalDivider()
            Text("Recent apps (last 5 min)", style = MaterialTheme.typography.titleMedium)
            if (!ua) Text("Needs usage access") else if(rc.isEmpty()) Text("Nothing yet")
        } }
        items(rc) { (p, t) ->
            Row(Modifier.fillMaxWidth()) { Text(Usg.lbl(c, p), Modifier.weight(1f)); Text(tf.format(Date(t))) }
        }
        item { Column { HorizontalDivider(); Text("Today", style = MaterialTheme.typography.titleMedium) } }
        items(td) { (p, ms) ->
            Row(Modifier.fillMaxWidth()){
                Text(Usg.lbl(c, p), Modifier.weight(1f))
                Text(fmt(ms))
            }
        }
    }
}

@Composable
private fun Prm(nm: String, ok: Boolean, fix: () -> Unit) {
  Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
      Text(nm)
      Text(if (ok) "Granted" else "Not granted", style = MaterialTheme.typography.bodySmall,
          color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
    }
    if (!ok) Button(fix) { Text("Grant") }
  }
}

fun fmt(ms: Long): String {
    val m = ms/60000
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}
