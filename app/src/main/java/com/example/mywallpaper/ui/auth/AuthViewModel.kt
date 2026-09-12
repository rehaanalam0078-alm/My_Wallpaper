package com.example.mywallpaper.ui.auth

import android.content.Context
import android.content.Intent
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywallpaper.R
import com.example.mywallpaper.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val context: Context) : ViewModel() {

    private val repo = AuthRepository(
        context = context,
        webClientId = try { context.getString(R.string.default_web_client_id) } catch (e: Exception) { "" }
    )

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val isAlreadyLoggedIn get() = repo.isLoggedIn

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    _state.value = AuthState.Error("Google sign-in failed: No ID token received")
                    return@launch
                }
                val result = repo.signInWithGoogle(idToken)
                _state.value = if (result.isSuccess) {
                    AuthState.Authenticated
                } else {
                    AuthState.Error(result.exceptionOrNull()?.message ?: "Google sign-in failed")
                }
            } catch (e: ApiException) {
                // Ignore user cancellation without showing an error banner
                if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED ||
                    e.statusCode == CommonStatusCodes.CANCELED) {
                    _state.value = AuthState.Idle
                } else {
                    _state.value = AuthState.Error(e.localizedMessage ?: "Google sign-in failed (${e.statusCode})")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            _state.value = AuthState.Error("Please enter your email address")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _state.value = AuthState.Error("Please enter a valid email address")
            return
        }
        if (password.isBlank()) {
            _state.value = AuthState.Error("Please enter your password")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repo.signInWithEmail(cleanEmail, password)
            _state.value = if (result.isSuccess) {
                AuthState.Authenticated
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Sign in failed"
                // Friendly error translation
                val friendlyMsg = when {
                    msg.contains("user-not-found", ignoreCase = true) -> "No account found with this email"
                    msg.contains("wrong-password", ignoreCase = true) ||
                    msg.contains("invalid-credential", ignoreCase = true) -> "Invalid email or password"
                    msg.contains("network", ignoreCase = true) -> "Network error. Please check your connection"
                    else -> msg
                }
                AuthState.Error(friendlyMsg)
            }
        }
    }

    fun signUp(name: String, email: String, phone: String, password: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.isBlank()) {
            _state.value = AuthState.Error("Please enter your full name")
            return
        }
        if (cleanEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _state.value = AuthState.Error("Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            _state.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repo.signUp(cleanName, cleanEmail, phone.trim(), password)
            _state.value = if (result.isSuccess) {
                AuthState.Authenticated
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repo.signOut()
            _state.value = AuthState.Idle
        }
    }

    fun getGoogleSignInClient() = repo.getGoogleSignInClient()

    fun resetState() {
        _state.value = AuthState.Idle
    }
}
