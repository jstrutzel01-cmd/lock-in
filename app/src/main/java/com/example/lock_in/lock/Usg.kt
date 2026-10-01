package com.example.lock_in.lock

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.net.toUri
import java.util.Calendar

object Usg {

    @Suppress("DEPRECATION")
    fun has(c: Context): Boolean {
        val ao = c.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val m = if (Build.VERSION.SDK_INT >= 29) ao.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.packageName)
                else ao.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.packageName)
        if (m == AppOpsManager.MODE_DEFAULT)
            return c.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        return m==AppOpsManager.MODE_ALLOWED
    }
    fun ovl(c: Context) = Settings.canDrawOverlays(c)

    fun gUsg(c: Context) = c.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    fun gOvl(c: Context) {
        c.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${c.packageName}".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun sm(c: Context) = c.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    @Suppress("DEPRECATION")
    fun rec(c: Context, win: Long = 300_000): List<Pair<String, Long>> {
        val now = System.currentTimeMillis()
        val ev = sm(c).queryEvents(now - win, now)
        val e = UsageEvents.Event()
        val l = ArrayList<Pair<String, Long>>()
        while (ev.hasNextEvent()) {
            ev.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) l.add(e.packageName to e.timeStamp)
        }
        return l.reversed().distinctBy { it.first }.take(8)
    }

    fun top(c: Context) = rec(c, 60_000).firstOrNull()?.first

    @Suppress("DEPRECATION")
    fun tdy(c: Context): List<Pair<String,Long>> {
        val k = Calendar.getInstance()
        k.set(Calendar.HOUR_OF_DAY,0); k.set(Calendar.MINUTE,0); k.set(Calendar.SECOND, 0); k.set(Calendar.MILLISECOND, 0)
        val now = System.currentTimeMillis()
        val ev = sm(c).queryEvents(k.timeInMillis, now)
        val e = UsageEvents.Event()
        val tot = HashMap<String, Long>()
        val from = HashMap<String, Long>()
        while (ev.hasNextEvent()) {
            ev.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) from.putIfAbsent(e.packageName, e.timeStamp)
            else if (e.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)
                from.remove(e.packageName)?.let { tot[e.packageName] = (tot[e.packageName] ?: 0) + e.timeStamp - it }
        }
        for ((p, t) in from) tot[p] = (tot[p] ?: 0) + now - t
        return tot.filter { it.value > 60000 }.toList().sortedByDescending { it.second }.take(15)
    }

    fun lbl(c: Context, p: String) = try {
        c.packageManager.run { getApplicationLabel(getApplicationInfo(p, 0)).toString() }
    } catch (x: PackageManager.NameNotFoundException) { p }
}
