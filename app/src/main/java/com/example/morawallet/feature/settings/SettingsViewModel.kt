package com.example.morawallet.feature.settings

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

data class SettingsUiState(
    val name: String = "",
    val email: String = "",
    val baseCurrency: String = "USD",
    val loading: Boolean = true,
    val error: String? = null,
    // Change-password dialog
    val changing: Boolean = false,
    val changeError: String? = null,
    val changeSuccess: Boolean = false,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    var state by mutableStateOf(SettingsUiState())
        private set

    private val uid: String? = authRepository.currentUserId()

    init {
        load()
    }

    fun load() {
        val id = uid
        if (id == null) {
            state = state.copy(loading = false, error = "You are not signed in")
            return
        }
        viewModelScope.launch {
            state = state.copy(loading = true)
            when (val result = userRepository.getUser(id)) {
                is Resource.Success -> state = state.copy(
                    loading = false,
                    name = result.data.displayName,
                    email = result.data.email,
                    baseCurrency = result.data.baseCurrency,
                    error = null,
                )

                is Resource.Error -> {
                    // Profile doc missing (e.g. account created before Firestore existed):
                    // self-heal by creating it from the auth account instead of showing an error.
                    val email = authRepository.currentEmail().orEmpty()
                    val fallbackName = email.substringBefore("@").ifBlank { "You" }
                    userRepository.createUserProfile(
                        uid = id,
                        email = email,
                        displayName = fallbackName,
                        baseCurrency = state.baseCurrency,
                    )
                    state = state.copy(
                        loading = false,
                        name = fallbackName,
                        email = email,
                        error = null,
                    )
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun setBaseCurrency(code: String) {
        val id = uid ?: return
        state = state.copy(baseCurrency = code)
        viewModelScope.launch { userRepository.updateBaseCurrency(id, code) }
    }

    fun changePassword(current: String, new: String, confirm: String) {
        val error = when {
            current.isBlank() -> "Enter your current password"
            Validators.passwordError(new) != null -> Validators.passwordError(new)
            Validators.confirmPasswordError(new, confirm) != null ->
                Validators.confirmPasswordError(new, confirm)

            else -> null
        }
        if (error != null) {
            state = state.copy(changeError = error)
            return
        }
        viewModelScope.launch {
            state = state.copy(changing = true, changeError = null)
            when (val result = authRepository.changePassword(current, new)) {
                is Resource.Success -> state = state.copy(changing = false, changeSuccess = true)
                is Resource.Error -> state = state.copy(changing = false, changeError = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun consumeChangeSuccess() {
        state = state.copy(changeSuccess = false, changeError = null)
    }

    fun logout() = authRepository.logout()
}
