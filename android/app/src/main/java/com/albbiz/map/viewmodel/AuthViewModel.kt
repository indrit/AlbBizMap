// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    fun login(email: String, password: String, isAlbanian: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = auth.currentUser
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseError(e, isAlbanian))
            }
        }
    }

    fun register(email: String, password: String, isAlbanian: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _currentUser.value = auth.currentUser
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseError(e, isAlbanian))
            }
        }
    }
    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun refreshCurrentUser() {
        // Firebase's updateProfile() already updates the local cached FirebaseUser
        // synchronously on success, so we don't need to wait or hit the network here —
        // just re-publish it. The null-then-reassign forces a distinct emission since
        // Firebase reuses the same FirebaseUser reference internally (reload() mutates
        // it in place), which would otherwise be deduped by StateFlow and not trigger
        // recomposition.
        _currentUser.value = null
        _currentUser.value = auth.currentUser
    }

    private fun mapFirebaseError(e: Exception, isAlbanian: Boolean): String {
        val errorCode = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: ""
        return if (isAlbanian) {
            when (errorCode) {
                "ERROR_INVALID_EMAIL" -> "Formati i emailit është i pavlefshëm."
                "ERROR_WRONG_PASSWORD" -> "Fjalëkalimi është i gabuar. Ju lutemi provoni përsëri."
                "ERROR_USER_NOT_FOUND" -> "Nuk u gjet asnjë llogari me këtë email."
                "ERROR_USER_DISABLED" -> "Kjo llogari është çaktivizuar."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Ky email është tashmë i regjistruar."
                "ERROR_WEAK_PASSWORD" -> "Fjalëkalimi duhet të ketë të paktën 6 karaktere."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Gabim rrjeti. Kontrolloni lidhjen tuaj."
                "ERROR_TOO_MANY_REQUESTS" -> "Shumë tentativa. Ju lutemi provoni më vonë."
                "ERROR_INVALID_CREDENTIAL" -> "Email ose fjalëkalim i gabuar. Provoni përsëri."
                else -> "Diçka shkoi keq. Ju lutemi provoni përsëri."
            }
        } else {
            when (errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address format."
                "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                "ERROR_USER_DISABLED" -> "This account has been disabled."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection."
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                "ERROR_INVALID_CREDENTIAL" -> "Incorrect email or password. Please try again."
                else -> "Something went wrong. Please try again."
            }
        }
    }
}