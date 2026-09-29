package com.example.lock_in.nav

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.lock_in.auth.Au
import com.example.lock_in.auth.Lgn
import com.example.lock_in.data.Prf
import com.example.lock_in.onb.Onb

object Rt {
    const val AUTH = "auth"; const val ONB = "onb"
    const val MAIN = "main"

    const val FOOD = "food"
    const val WRK="wrk"
    const val LOCK = "lock"
    const val USG = "usg"
}

fun st(c: Context): String {
    val u = Au.cur() ?: return Rt.AUTH
    return if (Prf.ok(c, u.uid)) Rt.MAIN else Rt.ONB
}

fun NavController.go(r: String) = navigate(r) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}

@Composable
fun Nav() {
    val c = LocalContext.current
    val nc = rememberNavController()
    val s0 = remember { st(c) }
    val out = { Au.out(); nc.go(Rt.AUTH) }

    NavHost(nc, s0) {
        composable(Rt.AUTH) { Lgn { nc.go(st(c)) } }
        composable(Rt.ONB) {
            Onb(done = { nc.go(Rt.MAIN) }, out = out)
        }
        composable(Rt.MAIN){ Home(out) }
    }
}
