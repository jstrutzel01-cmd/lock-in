package com.example.lock_in.nav

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.lock_in.food.Nut
import com.example.lock_in.lock.Lck
import com.example.lock_in.lock.UsgT
import com.example.lock_in.wrk.Wrk

private data class Tab(val rt: String, val lbl: String, val ic: String)
private val TABS = listOf(Tab(Rt.FOOD, "Nutrition", "🍎"), Tab(Rt.WRK, "Workouts", "🏋"),
    Tab(Rt.LOCK, "Lock", "🔒"))

@Composable
fun Home(out: () -> Unit) {
    val nc = rememberNavController()
    val be by nc.currentBackStackEntryAsState()
    val cur = be?.destination?.route


    Scaffold(topBar = {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Lock-In", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            TextButton(out) { Text("Sign out") }
        }
    }, bottomBar = {
        NavigationBar {
            for (t in TABS) NavigationBarItem(
                selected = cur == t.rt || (t.rt == Rt.LOCK && cur == Rt.USG),
                onClick = { nc.navigate(t.rt) { popUpTo(nc.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Text(t.ic) }, label = { Text(t.lbl) })
        }
    }) {p->
        NavHost(nc, Rt.FOOD, Modifier.padding(p)) {
            composable(Rt.FOOD) { Nut() }
            composable(Rt.WRK) { Wrk() }
            composable(Rt.LOCK) { Lck { nc.navigate(Rt.USG) } }
            composable(Rt.USG) { UsgT { nc.popBackStack() } }
        }
    }
}
