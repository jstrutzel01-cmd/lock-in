package com.example.lock_in.onb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lock_in.auth.Au
import com.example.lock_in.data.Prf
import com.example.lock_in.data.Usr

@Composable
fun Onb(done: () -> Unit, out: () -> Unit) {
    val c = LocalContext.current
    val fu = Au.cur()
    var nm by rememberSaveable { mutableStateOf(fu?.displayName ?: "") }
    var age by rememberSaveable { mutableStateOf("") }
    var male by rememberSaveable { mutableStateOf(true) }
    var imp by rememberSaveable { mutableStateOf(true) }
    var ft by rememberSaveable { mutableStateOf("") }
    var inch by rememberSaveable { mutableStateOf("") }
    var cmS by rememberSaveable { mutableStateOf("") }
    var wtS by rememberSaveable { mutableStateOf("") }
    var act by rememberSaveable { mutableStateOf(Act.MOD) }
    var goal by rememberSaveable { mutableStateOf(Goal.KEEP) }

    val a = age.toIntOrNull()
    val cm = if (imp) ft.toDoubleOrNull()?.let { Calc.ftCm(it, inch.toDoubleOrNull() ?: 0.0) } else cmS.toDoubleOrNull()
    val kg = wtS.toDoubleOrNull()?.let { if (imp) Calc.lbKg(it) else it }
    val t = if (nm.isNotBlank() && Calc.ok(a, kg, cm)) Calc.tgt(male, kg!!, cm!!, a!!, act, goal) else null

    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Let's set you up", style = MaterialTheme.typography.headlineMedium)
        Text("We use this to work out your daily calories and macros.")

        OutlinedTextField(nm, { nm = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
        Num(age, { age = it.filter(Char::isDigit).take(3) }, "Age", Modifier.fillMaxWidth(), false)

        Hdr("Sex")
        Row { Opt("Male", male, Modifier.weight(1f)) { male = true }; Opt("Female", !male, Modifier.weight(1f)) { male = false } }

        Hdr("Units")
        Row {
            Opt("lb / ft", imp, Modifier.weight(1f)) { imp = true }
            Opt("kg / cm", !imp, Modifier.weight(1f)) { imp = false }
        }
        if (imp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Num(ft, { ft = it }, "Height (ft)", Modifier.weight(1f))
                Num(inch, {inch = it}, "(in)", Modifier.weight(1f))
            }
            Num(wtS, { wtS = it }, "Weight (lb)", Modifier.fillMaxWidth())
        } else {
          Num(cmS, { cmS = it }, "Height (cm)", Modifier.fillMaxWidth())
          Num(wtS, { wtS = it }, "Weight (kg)", Modifier.fillMaxWidth())
        }

        Hdr("Activity level")
        Act.entries.forEach { x -> Opt(x.lbl, act == x, Modifier.fillMaxWidth(), x.sub) { act = x } }


        Hdr("Goal")
        for (g in Goal.entries) Opt(g.lbl, goal==g, Modifier.fillMaxWidth()) { goal = g }

        if (t != null) TCrd(t)
        else if (age.isNotEmpty() && wtS.isNotEmpty()) Text("Double check your age, height and weight", color = MaterialTheme.colorScheme.error)

        Button(onClick = {
            if (t == null || fu == null) return@Button
            Prf.sv(c, Usr(fu.uid, nm.trim(), fu.email ?: "", a!!, if (male) "M" else "F", cm!!, kg!!, act.name, goal.name,
                t.kcal, t.pro, t.carb, t.fat, true, System.currentTimeMillis()))
            done()
        }, modifier = Modifier.fillMaxWidth(), enabled = t != null) { Text("Save and continue") }
        TextButton(out, Modifier.align(Alignment.CenterHorizontally)) { Text("Not you? Sign out") }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun Hdr(s: String) = Text(s, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.titleMedium)

@Composable
private fun Num(v: String, set: (String) -> Unit, lbl: String, md: Modifier = Modifier, dec: Boolean = true) =
    OutlinedTextField(v, { s -> if (s.count { it == '.' } <= 1) set(s.filter { it.isDigit() || (dec && it == '.') }.take(6)) }, md,
        label = { Text(lbl) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (dec) KeyboardType.Decimal else KeyboardType.Number))

@Composable
private fun Opt(txt: String, sel: Boolean, md: Modifier = Modifier, sub: String? = null, pk: () -> Unit) {
    Row(md.selectable(sel, onClick = pk, role = Role.RadioButton).padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(sel, null)
        Column(Modifier.padding(start = 6.dp)) {
            Text(txt)
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TCrd(t: Tgt) {
  Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
      Text("Your daily targets", style = MaterialTheme.typography.titleMedium)
      Text("${t.kcal} kcal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Mac("Protein", t.pro); Mac("Carbs", t.carb); Mac("Fat", t.fat)
      }
      Text("BMR " + t.bmr + "  |  maintenance " + t.tdee, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
    }
  }
}

@Composable
private fun Mac(nm: String, g: Int) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("${g}g", style = MaterialTheme.typography.titleLarge)
    Text(nm, style = MaterialTheme.typography.bodySmall)
}
