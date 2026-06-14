package com.example.morawallet.feature.wallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.WalletRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class WalletsUiState(
    val loading: Boolean = true,
    val wallets: List<Wallet> = emptyList(),
    val error: String? = null,
)

class WalletListViewModel(
    private val walletRepository: WalletRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(WalletsUiState())
        private set

    private val uid = authRepository.currentUserId()

    init {
        val id = uid
        if (id == null) {
            state = state.copy(loading = false, error = "You are not signed in")
        } else {
            viewModelScope.launch {
                walletRepository.observeWallets(id)
                    .catch { e -> state = state.copy(loading = false, error = e.localizedMessage) }
                    .collect { wallets ->
                        state = state.copy(loading = false, wallets = wallets, error = null)
                    }
            }
        }
    }
}
