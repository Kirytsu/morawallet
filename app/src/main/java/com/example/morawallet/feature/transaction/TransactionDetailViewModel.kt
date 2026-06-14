package com.example.morawallet.feature.transaction

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
import kotlinx.coroutines.launch

data class TxnDetailUiState(
    val loading: Boolean = true,
    val transaction: Transaction? = null,
    val walletsById: Map<String, Wallet> = emptyMap(),
    val error: String? = null,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
)

class TransactionDetailViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val txnId: String,
) : ViewModel() {

    var state by mutableStateOf(TxnDetailUiState())
        private set

    private val uid = authRepository.currentUserId()

    init {
        load()
    }

    private fun load() {
        val id = uid
        if (id == null) {
            state = state.copy(loading = false, error = "You are not signed in")
            return
        }
        viewModelScope.launch {
            when (val txnResult = transactionRepository.getTransaction(id, txnId)) {
                is Resource.Success -> state = state.copy(loading = false, transaction = txnResult.data)
                is Resource.Error -> state = state.copy(loading = false, error = txnResult.message)
                Resource.Loading -> Unit
            }
        }
        viewModelScope.launch {
            walletRepository.observeWallets(id).collect { wallets ->
                state = state.copy(walletsById = wallets.associateBy { it.id })
            }
        }
    }

    fun delete() {
        val id = uid ?: return
        val txn = state.transaction ?: return
        viewModelScope.launch {
            state = state.copy(deleting = true)
            when (val result = transactionRepository.deleteTransaction(id, txn)) {
                is Resource.Success -> state = state.copy(deleting = false, deleted = true)
                is Resource.Error -> state = state.copy(deleting = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }
}
