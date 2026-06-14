package com.example.morawallet.feature.wallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.WalletRepository
import kotlinx.coroutines.launch

data class WalletFormUiState(
    val name: String = "",
    val currencyCode: String = "USD",
    val initialBalance: String = "",
    val colorIndex: Int = 0,
    val isEdit: Boolean = false,
    val nameError: String? = null,
    val balanceError: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class WalletFormViewModel(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val walletId: String?,
) : ViewModel() {

    var state by mutableStateOf(WalletFormUiState(isEdit = walletId != null))
        private set

    private var existing: Wallet? = null
    private val uid = authRepository.currentUserId()

    init {
        if (walletId != null) loadExisting(walletId)
    }

    private fun loadExisting(id: String) {
        val userId = uid ?: return
        viewModelScope.launch {
            when (val result = walletRepository.getWallet(userId, id)) {
                is Resource.Success -> {
                    existing = result.data
                    state = state.copy(
                        name = result.data.name,
                        currencyCode = result.data.currencyCode,
                        colorIndex = result.data.colorIndex,
                    )
                }

                is Resource.Error -> state = state.copy(error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value, nameError = null, error = null)
    }

    fun onCurrencyChange(code: String) {
        state = state.copy(currencyCode = code)
    }

    fun onInitialBalanceChange(value: String) {
        state = state.copy(initialBalance = value, balanceError = null)
    }

    fun onColorChange(index: Int) {
        state = state.copy(colorIndex = index)
    }

    fun save() {
        val userId = uid
        if (userId == null) {
            state = state.copy(error = "You are not signed in")
            return
        }
        val nameError = if (state.name.isBlank()) "Name is required" else null
        val balance = if (state.isEdit) {
            existing?.balance ?: 0.0
        } else {
            if (state.initialBalance.isBlank()) {
                0.0
            } else {
                state.initialBalance.replace(",", "").trim().toDoubleOrNull().also {
                    if (it == null) {
                        state = state.copy(balanceError = "Enter a valid amount")
                        return
                    }
                } ?: 0.0
            }
        }
        if (nameError != null) {
            state = state.copy(nameError = nameError)
            return
        }

        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            val result = if (state.isEdit) {
                val base = existing ?: Wallet(id = walletId.orEmpty())
                walletRepository.updateWallet(
                    userId,
                    base.copy(
                        name = state.name.trim(),
                        currencyCode = state.currencyCode,
                        colorIndex = state.colorIndex,
                    ),
                )
            } else {
                walletRepository.createWallet(
                    userId,
                    Wallet(
                        name = state.name.trim(),
                        currencyCode = state.currencyCode,
                        balance = balance,
                        colorIndex = state.colorIndex,
                    ),
                )
            }
            state = when (result) {
                is Resource.Success<*> -> state.copy(loading = false, success = true)
                is Resource.Error -> state.copy(loading = false, error = result.message)
                Resource.Loading -> state
            }
        }
    }
}
