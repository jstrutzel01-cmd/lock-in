package com.example.lock_in.auth

private val RX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun okEm(em: String) = RX.matches(em.trim())

fun chk(su:Boolean, nm:String, em:String, pw:String, pw2:String): String? {
    if (su && nm.isBlank()) return "Enter your name"
    if(!okEm(em)) return "Enter a valid email"
    if (pw.length<6) return "Password needs 6+ characters"
    if (su && pw != pw2) return "Passwords don't match"
    return null
}
