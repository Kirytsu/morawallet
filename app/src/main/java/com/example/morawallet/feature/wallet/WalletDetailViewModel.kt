package com.example.morawallet.feature.wallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.TransactionRepository
import com.example.morawallet.data.repository.WalletRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WalletDetailUiState(
    val loading: Boolean = true,
    val wallet: Wallet? = null,
    val transactions: List<Transaction> = emptyList(),
    val walletsById: Map<String, Wallet> = emptyMap(),
    val error: String? = null,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
)

class WalletDetailViewModel(
    private val walletRepository: WalletRepository,
    transactionRepository: TransactionRepository,
    authRepository: AuthRepository,
    private val walletId: String,
) : ViewModel() {

    var state by mutableStateOf(WalletDetailUiState())
        private set

    private val uid = authRepository.currentUserId()

    init {
        val id = uid
        if (id == null) {
            state = state.copy(loading = false, error = "You are not signed in")
        } else {
            viewModelScope.launch {
                combine(
                    walletRepository.observeWallets(id),
                    transactionRepository.observeTransactions(id),
                ) { wallets, txns -> wallets to txns }
                    .catch { e -> state = state.copy(loading = false, error = e.localizedMessage) }
                    .collect { (wallets, txns) ->
                        state = state.copy(
                            loading = false,
                            wallet = wallets.find { it.id == walletId },
                            transactions = txns.filter {
                                it.walletId == walletId || it.toWalletId == walletId
                            },
                            walletsById = wallets.associateBy { it.id },
                        )
                    }
            }
        }
    }

    fun delete() {
        val id = uid ?: return
        viewModelScope.launch {
            state = state.copy(deleting = true)
            when (val result = walletRepository.deleteWallet(id, walletId)) {
                is Resource.Success -> state = state.copy(deleting = false, deleted = true)
                is Resource.Error -> state = state.copy(deleting = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }
}
