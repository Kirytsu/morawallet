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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val loading: Boolean = false,
    val googleLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    var state by mutableStateOf(RegisterUiState())
        private set

    fun onNameChange(value: String) {
        state = state.copy(name = value, nameError = null, error = null)
    }

    fun onEmailChange(value: String) {
        state = state.copy(email = value, emailError = null, error = null)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, passwordError = null, error = null)
    }

    fun onConfirmChange(value: String) {
        state = state.copy(confirm = value, confirmError = null, error = null)
    }

    fun register() {
        val nameError = Validators.requiredError(state.name, "Name")
        val emailError = Validators.emailError(state.email)
        val passwordError = Validators.passwordError(state.password)
        val confirmError = Validators.confirmPasswordError(state.password, state.confirm)
        if (nameError != null || emailError != null || passwordError != null || confirmError != null) {
            state = state.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmError = confirmError,
            )
            return
        }
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            when (val result = authRepository.register(state.email, state.password)) {
                is Resource.Success -> {
                    // Best-effort profile creation; the account already exists either way.
                    userRepository.createUserProfile(
                        uid = result.data,
                        email = state.email.trim(),
                        displayName = state.name.trim(),
                    )
                    state = state.copy(loading = false, success = true)
                }

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
