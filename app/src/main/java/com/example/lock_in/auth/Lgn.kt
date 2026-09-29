package com.example.lock_in.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun Lgn(done: () -> Unit) {
  var su by rememberSaveable { mutableStateOf(false) }
  var nm by rememberSaveable { mutableStateOf("") }
  var em by rememberSaveable{ mutableStateOf("") }
  var pw by remember { mutableStateOf("") }
  var pw2 by remember { mutableStateOf("") }
  var err by remember { mutableStateOf<String?>(null) }
  var ok by remember { mutableStateOf<String?>(null) }
  var bsy by remember { mutableStateOf(false) }
  val pwt = PasswordVisualTransformation()

  Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Spacer(Modifier.height(48.dp))
    Text("Lock-In", style = MaterialTheme.typography.displaySmall)
    Text(if(su) "Create your account" else "Sign in to keep going")
    Spacer(Modifier.height(24.dp))

    if (su) OutlinedTextField(nm, { nm = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
    OutlinedTextField(em, {em = it.trim()}, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
    OutlinedTextField(pw, { pw = it }, Modifier.fillMaxWidth(),
        label = { Text("Password") }, singleLine = true, visualTransformation = pwt,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
    if(su) {
        OutlinedTextField(pw2, { pw2 = it }, Modifier.fillMaxWidth(), label = { Text("Confirm password") }, singleLine = true,
          visualTransformation = pwt, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
    }

    if (err != null) Text(err!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
    ok?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top=8.dp)) }
    Spacer(Modifier.height(16.dp));

    Button({
        val e = chk(su, nm, em, pw, pw2)
        if (e != null) { err = e; return@Button }
        err = null; ok = null; bsy = true
        val cb: (String?) -> Unit = { m -> bsy = false; if (m == null) done() else err = m }
        if (su) Au.reg(nm.trim(), em, pw, cb) else Au.inn(em, pw, cb)
    }, Modifier.fillMaxWidth(), enabled = !bsy) {
        Text(if (bsy) "Please wait..." else if (su) "Create account" else "Sign in")
    }
    TextButton(onClick = { su = !su; err = null; ok = null }) {
        Text(if (su) "Already have an account? Sign in" else "New here? Create an account")
    }
    if (!su) TextButton(onClick = {
        if(!okEm(em)) { err = "Enter your email first"; return@TextButton }
        Au.rst(em) { m -> if (m == null) { ok = "Reset link sent to $em"; err = null } else err = m }
    }) { Text("Forgot password?") }
  }
}
