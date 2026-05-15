package com.nammahomestay.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthException
import com.nammahomestay.firebase.FirebaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                FirebaseManager.auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val message = if (e is FirebaseAuthException) e.errorCode else e.localizedMessage
                _authState.value = AuthState.Error(message ?: "Login failed")
            }
        }
    }

    fun register(name: String, phone: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = FirebaseManager.auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("User ID is null")

                val hostData = hashMapOf(
                    "name" to name,
                    "phone" to phone,
                    "email" to email,
                    "createdAt" to Timestamp.now(),
                    "isProfileComplete" to false
                )

                FirebaseManager.firestore.collection("hosts").document(uid).set(hostData).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val message = if (e is FirebaseAuthException) e.errorCode else e.localizedMessage
                _authState.value = AuthState.Error(message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                FirebaseManager.auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Error("Password reset email sent!") 
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                val message = if (e is FirebaseAuthException) e.errorCode else e.localizedMessage
                _authState.value = AuthState.Error(message ?: "Failed to send reset email")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
