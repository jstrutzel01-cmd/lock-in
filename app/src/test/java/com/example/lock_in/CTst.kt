package com.example.lock_in

import com.example.lock_in.auth.chk
import com.example.lock_in.auth.okEm
import com.example.lock_in.onb.Act
import com.example.lock_in.onb.Calc
import com.example.lock_in.onb.Goal
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class CTst {

    @Test fun bmrM() = assertEquals(1805.0, Calc.bmr(true, 80.0, 180.0, 25), 0.01)
    @Test
    fun bmrF() { assertEquals(1320.25, Calc.bmr(false, 60.0, 165.0, 30), 0.01) }

    @Test
    fun keep() {
        val t = Calc.tgt(true, 80.0, 180.0, 25, Act.MOD, Goal.KEEP)
        assertEquals(1805, t.bmr); assertEquals(2798, t.tdee)
        assertEquals(2798, t.kcal)
        assertEquals(128, t.pro)
        assertEquals(78, t.fat); assertEquals(396, t.carb)
    }

    @Test
    fun sum() {
        for (g in Goal.entries) for (a in Act.entries) {
            val t = Calc.tgt(false, 70.0, 170.0, 22, a, g)
            val s = t.pro*4 + t.carb*4 + t.fat*9
            assertTrue("$g $a $s ${t.kcal}", abs(s - t.kcal) <= 10)
        }
    }

    @Test fun cut() {
        val k = Calc.tgt(true, 80.0, 180.0, 25, Act.MOD, Goal.KEEP).kcal
        assertEquals(k - 500, Calc.tgt(true, 80.0, 180.0, 25, Act.MOD, Goal.CUT).kcal)
        assertEquals(k+300, Calc.tgt(true, 80.0, 180.0, 25, Act.MOD, Goal.BULK).kcal)
    }

    @Test
    fun flr() {
        assertEquals(1200, Calc.tgt(false, 40.0, 150.0, 60, Act.SED, Goal.CUT).kcal)
        assertEquals(1500, Calc.tgt(true, 45.0, 150.0, 70, Act.SED, Goal.CUT).kcal)
    }

    @Test
    fun unit() {
        assertEquals(45.36, Calc.lbKg(100.0), 0.01)
        assertEquals(177.8, Calc.ftCm(5.0, 10.0), 0.01)
    }

    @Test fun rng() {
        assertTrue(Calc.ok(20, 70.0, 175.0))
        assertFalse(Calc.ok(null, 70.0, 175.0)); assertFalse(Calc.ok(8, 70.0, 175.0))
        assertFalse(Calc.ok(20, 900.0, 175.0))
        assertFalse(Calc.ok(20, 70.0, 17.5))
    }

    @Test
    fun auth() {
        assertTrue(okEm("mitt@test.com"))
        assertFalse(okEm("mitt@test"))
        assertNull(chk(false, "", "a@b.co", "secret1", ""))
        assertNotNull(chk(true, "", "a@b.co", "secret1", "secret1"))
        assertNotNull(chk(true, "Mitt", "a@b.co", "secret1", "secret2"))
        assertNotNull(chk(false, "", "a@b.co", "123", ""))
        assertNull(chk(true, "Mitt", "a@b.co", "secret1", "secret1"))
    }
}
