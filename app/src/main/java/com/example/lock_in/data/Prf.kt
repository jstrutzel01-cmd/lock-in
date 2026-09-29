package com.example.lock_in.data

import android.content.Context
import androidx.core.content.edit

object Prf {
    private const val F = "lockin_profile"
    private fun sp(c: Context) = c.getSharedPreferences(F, Context.MODE_PRIVATE)

    fun sv(c: Context, u: Usr) = sp(c).edit {
        putString("uid", u.uid); putString("nm", u.nm); putString("em",u.em)
        putInt("age", u.age); putString("sex", u.sex)
        putFloat("ht", u.ht.toFloat())
        putFloat("wt", u.wt.toFloat())
        putString("act",u.act);putString("goal",u.goal)
        putInt("kcal", u.kcal); putInt("pro", u.pro); putInt("carb", u.carb); putInt("fat", u.fat)
        putBoolean("onb", u.onb); putLong("ts", u.ts)
    }

    fun ld(c: Context): Usr? {
        val s = sp(c)
        val id = s.getString("uid", null) ?: return null
        return Usr(id, s.getString("nm","")!!, s.getString("em","")!!, s.getInt("age",0), s.getString("sex","M")!!,
            s.getFloat("ht",0f).toDouble(), s.getFloat("wt", 0f).toDouble(),
            s.getString("act","MOD")!!, s.getString("goal","KEEP")!!,
            s.getInt("kcal",0), s.getInt("pro",0), s.getInt("carb",0), s.getInt("fat",0),
            s.getBoolean("onb",false), s.getLong("ts",0))
    }

    fun ok(c: Context, id: String) : Boolean {
        val u = ld(c) ?: return false
        return u.uid == id && u.onb
    }
    fun clr(c: Context) = sp(c).edit { clear() }
}
