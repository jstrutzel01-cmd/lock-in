package com.example.lock_in.food

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NutViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

    fun saveMealToFirebase(meal: Meal) {
        val userID = auth.currentUser?.uid ?: return

        db.collection("users").document(userID)
            .collection("nutrition").add(meal)
            .addOnSuccessListener {
                fetchMealsFromFirebase()
            }
    }

    // Fetches meals from DB and sorts them by when they were uploaded
    fun fetchMealsFromFirebase() {
        val userID = auth.currentUser?.uid ?: return

        db.collection("users").document(userID).collection("nutrition")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedMeals = snapshot.toObjects(Meal::class.java)
                    _meals.value = fetchedMeals
                }
            }
    }

    // Deletes meal in DB
    fun deleteMeal(mealId: String){
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("nutrition")
            .document(mealId)
            .delete()
    }
}

