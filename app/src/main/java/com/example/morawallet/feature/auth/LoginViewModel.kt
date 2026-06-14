package com.example.morawallet.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Resource
import com.example.morawallet.core.util.Validators
import com.example.morawallet.data.repository.AuthRepository
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
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
}
