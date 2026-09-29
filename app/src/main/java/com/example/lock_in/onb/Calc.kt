package com.example.lock_in.onb

import kotlin.math.max
import kotlin.math.roundToInt

enum class Act(val lbl: String, val sub: String, val mul: Double) {
    SED("Sedentary", "Little or no exercise", 1.2),
    LITE("Lightly active", "Exercise 1-3 days a week", 1.375),
    MOD("Moderately active","Exercise 3-5 days a week", 1.55),
    HIGH("Very active", "Exercise 6-7 days a week", 1.725),
    XTRA("Extra active", "Hard training or a physical job", 1.9)
}

enum class Goal(val lbl: String, val adj: Int, val gpk: Double) { CUT("Lose weight", -500, 2.0), KEEP("Maintain", 0, 1.6), BULK("Build muscle", 300, 1.8) }

data class Tgt(val kcal: Int, val pro: Int, val carb: Int, val fat: Int, val bmr: Int, val tdee: Int)

object Calc {
    fun lbKg(lb: Double) = lb * 0.45359237
    fun inCm(i:Double)= i * 2.54
    fun ftCm(ft: Double, i: Double) = inCm(ft*12 + i)

    //mifflin st jeor
    fun bmr(m: Boolean, kg: Double, cm: Double, age: Int) = 10*kg + 6.25*cm - 5*age + (if (m) 5 else -161)

    fun tgt(m: Boolean, kg: Double, cm: Double, age: Int, a: Act, g: Goal): Tgt {
        val b = bmr(m, kg, cm, age)
        val td = b*a.mul
        val k = max((td + g.adj).roundToInt(), if (m) 1500 else 1200)

        val p = (kg * g.gpk).roundToInt()
        val f = (k*0.25/9).roundToInt()
        val cb = ((k - p*4 - f*9) / 4.0).roundToInt().coerceAtLeast(0)
        return Tgt(k, p, cb, f, b.roundToInt(), td.roundToInt())
    }

    fun ok(age: Int?, kg: Double?, cm: Double?) = age != null && kg != null && cm != null && age in 13..100 && kg in 30.0..300.0 && cm in 120.0..230.0
}
