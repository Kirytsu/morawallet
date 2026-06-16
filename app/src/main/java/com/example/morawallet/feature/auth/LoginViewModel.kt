package com.example.morawallet.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Resource
import com.example.morawallet.core.util.Validators
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.UserRepository
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loading: Boolean = false,
    val googleLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        state = state.copy(email = value, emailError = null, error = null)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, passwordError = null, error = null)
    }

    fun login() {
        val emailError = Validators.emailError(state.email)
        val passwordError = if (state.password.isBlank()) "Password is required" else null
        if (emailError != null || passwordError != null) {
            state = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            when (val result = authRepository.login(state.email, state.password)) {
                is Resource.Success -> state = state.copy(loading = false, success = true)
                is Resource.Error -> state = state.copy(loading = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    /** Called when the Google credential picker is launched, before a token arrives. */
    fun onGoogleStart() {
        state = state.copy(googleLoading = true, error = null)
    }

    /** Called when the user dismisses the Google picker. */
    fun onGoogleCancelled() {
        state = state.copy(googleLoading = false)
    }

    fun onGoogleError(message: String) {
        state = state.copy(googleLoading = false, error = message)
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            state = state.copy(googleLoading = true, error = null)
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Resource.Success -> {
                    val outcome = result.data
                    if (outcome.isNewUser) {
                        userRepository.createUserProfile(
                            uid = outcome.uid,
                            email = outcome.email,
                            displayName = outcome.displayName.ifBlank {
                                outcome.email.substringBefore("@")
                            },
                        )
                    }
                    state = state.copy(googleLoading = false, success = true)
                }

                is Resource.Error -> state = state.copy(googleLoading = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }
}
