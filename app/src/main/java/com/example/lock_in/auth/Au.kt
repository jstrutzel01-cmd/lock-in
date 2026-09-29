package com.example.lock_in.auth

import com.google.firebase.auth.*

object Au {
    private val fa get() = FirebaseAuth.getInstance()

    fun cur() = fa.currentUser

    fun inn(em: String, pw: String, cb: (String?) -> Unit) {
        fa.signInWithEmailAndPassword(em, pw).addOnCompleteListener { t -> cb(if (t.isSuccessful) null else msg(t.exception)) }
    }

    fun reg(nm: String, em:String, pw:String, cb:(String?)->Unit){
        fa.createUserWithEmailAndPassword(em,pw).addOnCompleteListener { t ->
            if(!t.isSuccessful){ cb(msg(t.exception)); return@addOnCompleteListener }
            val u = t.result?.user
            if (u == null) { cb(null); return@addOnCompleteListener }
            u.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nm).build()).addOnCompleteListener { cb(null) }
        }
    }

    fun rst(em: String, cb: (String?) -> Unit) = fa.sendPasswordResetEmail(em).addOnCompleteListener { t ->
        cb(if (t.isSuccessful) null else msg(t.exception))
    }

    fun out() = fa.signOut()

    private fun msg(e: Exception?) = when (e) {
        is FirebaseAuthWeakPasswordException -> "Password is too weak"
        is FirebaseAuthUserCollisionException -> "That email already has an account"
        is FirebaseAuthInvalidUserException -> "No account with that email"
        is FirebaseAuthInvalidCredentialsException -> "Wrong email or password"
        else -> e?.message ?: "Something went wrong"
    }
}
