package com.example.freshcut.utilities


import android.widget.Toast
import com.example.freshcut.objects.Appointment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth


object AuthManager {
    private val auth: FirebaseAuth = Firebase.auth

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun logIn(email: String, password: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(task.result.user?.uid ?: "")
                } else {
                    val exception = task.exception ?: Exception("Unknown authentication error")
                    onFailure(exception)
                }
            }
    }
    // Sign out logic
    fun signOut() {
        auth.signOut()
    }

    fun signUp(email: String, password: String, name: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            onSuccess(user.uid)
                        } else {
                            onFailure(profileTask.exception ?: Exception("Failed to update profile name"))
                        }
                    }
                } else {
                    onFailure(task.exception ?: Exception("Sign up failed"))
                }
            }
    }
    // Check if user is authenticated
    fun isUserLoggedIn(): Boolean = auth.currentUser != null


}