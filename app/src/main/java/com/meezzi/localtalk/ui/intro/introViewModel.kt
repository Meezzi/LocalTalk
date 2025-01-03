package com.meezzi.localtalk.ui.intro

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import com.meezzi.localtalk.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _authState = MutableStateFlow(auth.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState

    fun signInWithGoogle(context: Context) {

        viewModelScope.launch {
            try {
                val user = authRepository.signInWithGoogle(context)
                _authState.value = user
            } catch (e: Exception) {

            }
        }
    }

    fun signOutWithGoogle() {

        viewModelScope.launch {
            authRepository.signOutWithGoogle()
            _authState.value = null
        }
    }

    suspend fun hasUserData(): Boolean {
        val user = _authState.value ?: return false
        var hasUserData = false

        val docRef = db.collection("profiles").document(user.uid)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data?.get("nickname") != null) {
                    hasUserData = true
                }
            }.addOnFailureListener { exception ->
                hasUserData = false
            }.await()

        return hasUserData
    }
}