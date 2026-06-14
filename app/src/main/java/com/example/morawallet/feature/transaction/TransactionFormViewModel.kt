package com.example.morawallet.feature.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Categories
import com.example.morawallet.core.util.DateUtils
import com.example.morawallet.core.util.Resource
import com.example.morawallet.core.util.Validators
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.TransactionRepository
import com.example.morawallet.data.repository.WalletRepository
import kotlinx.coroutines.launch

data class TxnFormUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val walletId: String? = null,
    val toWalletId: String? = null,
    val category: String = "",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val receivedAmount: String = "",
    val wallets: List<Wallet> = emptyList(),
    val isEdit: Boolean = false,
    val amountError: String? = null,
    val walletError: String? = null,
    val toWalletError: String? = null,
    val categoryError: String? = null,
    val dateError: String? = null,
    val receivedError: String? = null,
    val loadingWallets: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
) {
    val walletsById: Map<String, Wallet> get() = wallets.associateBy { it.id }
    val sourceCurrency: String? get() = walletsById[walletId]?.currencyCode
    val destCurrency: String? get() = walletsById[toWalletId]?.currencyCode
    val crossCurrencyTransfer: Boolean
        get() = type == TransactionType.TRANSFER &&
            sourceCurrency != null && destCurrency != null && sourceCurrency != destCurrency
}

class TransactionFormViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    initialType: String?,
    private val txnId: String?,
) : ViewModel() {

    var state by mutableStateOf(
        TxnFormUiState(
            type = runCatching { TransactionType.valueOf(initialType ?: "") }
                .getOrDefault(TransactionType.EXPENSE),
            isEdit = txnId != null,
        ),
    )
        private set

    private var oldTxn: Transaction? = null
    private val uid = authRepository.currentUserId()

    init {
        observeWallets()
        if (txnId != null) loadTransaction(txnId)
    }

    private fun observeWallets() {
        val id = uid ?: return
        viewModelScope.launch {
            walletRepository.observeWallets(id).collect { wallets ->
                state = state.copy(
                    wallets = wallets,
                    loadingWallets = false,
                    walletId = state.walletId ?: wallets.firstOrNull()?.id,
                    toWalletId = state.toWalletId
                        ?: wallets.getOrNull(1)?.id ?: wallets.firstOrNull()?.id,
                )
                if (state.category.isBlank() && state.type != TransactionType.TRANSFER) {
                    state = state.copy(category = Categories.forType(state.type).first().name)
                }
            }
        }
    }

    private fun loadTransaction(id: String) {
        val userId = uid ?: return
        viewModelScope.launch {
            when (val result = transactionRepository.getTransaction(userId, id)) {
                is Resource.Success -> {
                    val t = result.data
                    oldTxn = t
                    state = state.copy(
                        type = t.typeEnum,
                        amount = formatAmount(t.amount),
                        walletId = t.walletId,
                        toWalletId = t.toWalletId,
                        category = t.category,
                        note = t.note,
                        date = t.date,
                        receivedAmount = t.convertedAmount?.let { formatAmount(it) } ?: "",
                    )
                }

                is Resource.Error -> state = state.copy(error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun onTypeChange(type: TransactionType) {
        val category = if (type == TransactionType.TRANSFER) {
            ""
        } else {
            Categories.forType(type).first().name
        }
        state = state.copy(type = type, category = category, categoryError = null)
    }

    fun onAmountChange(value: String) {
        state = state.copy(amount = value, amountError = null, error = null)
    }

    fun onWalletChange(walletId: String) {
        state = state.copy(walletId = walletId, walletError = null)
    }

    fun onToWalletChange(walletId: String) {
        state = state.copy(toWalletId = walletId, toWalletError = null)
    }

    fun onCategoryChange(category: String) {
        state = state.copy(category = category, categoryError = null)
    }

    fun onNoteChange(value: String) {
        state = state.copy(note = value)
    }

    fun onDateChange(millis: Long) {
        val combined = DateUtils.withDatePreservingTime(millis, state.date)
        state = if (DateUtils.isFutureDay(combined)) {
            state.copy(dateError = "Records cannot be dated in the future")
        } else {
            state.copy(date = combined, dateError = null, error = null)
        }
    }

    fun onTimeChange(hour: Int, minute: Int) {
        val combined = DateUtils.withTime(state.date, hour, minute)
        state = state.copy(date = combined, dateError = null, error = null)
    }

    fun onReceivedChange(value: String) {
        state = state.copy(receivedAmount = value, receivedError = null)
    }

    fun save() {
        val userId = uid
        if (userId == null) {
            state = state.copy(error = "You are not signed in")
            return
        }
        val amount = Validators.parsePositiveAmount(state.amount)
        val walletId = state.walletId
        var hasError = false
        var next = state.copy(
            amountError = null,
            walletError = null,
            toWalletError = null,
            categoryError = null,
            dateError = null,
            receivedError = null,
        )

        if (amount == null) {
            next = next.copy(amountError = "Enter a valid amount"); hasError = true
        }
        if (DateUtils.isFutureDay(state.date)) {
            next = next.copy(dateError = "Records cannot be dated in the future"); hasError = true
        }
        if (walletId == null) {
            next = next.copy(walletError = "Select a wallet"); hasError = true
        }

        var received: Double? = null
        when (state.type) {
            TransactionType.TRANSFER -> {
                val to = state.toWalletId
                if (to == null) {
                    next = next.copy(toWalletError = "Select a destination"); hasError = true
                } else if (to == walletId) {
                    next = next.copy(toWalletError = "Choose a different wallet"); hasError = true
                }
                if (state.crossCurrencyTransfer) {
                    received = Validators.parsePositiveAmount(state.receivedAmount)
                    if (received == null) {
                        next = next.copy(receivedError = "Enter the amount received"); hasError = true
                    }
                } else {
                    received = amount
                }
            }

            else -> {
                if (state.category.isBlank()) {
                    next = next.copy(categoryError = "Select a category"); hasError = true
                }
            }
        }

        if (hasError || amount == null || walletId == null) {
            state = next
            return
        }

        val sourceCurrency = state.walletsById[walletId]?.currencyCode.orEmpty()
        val isTransfer = state.type == TransactionType.TRANSFER
        val convertedAmount = if (isTransfer) (received ?: amount) else null

        val newTxn = Transaction(
            id = oldTxn?.id.orEmpty(),
            type = state.type.name,
            amount = amount,
            currencyCode = sourceCurrency,
            walletId = walletId,
            toWalletId = if (isTransfer) state.toWalletId else null,
            toCurrencyCode = if (isTransfer) state.destCurrency else null,
            convertedAmount = convertedAmount,
            fxRate = if (isTransfer && convertedAmount != null) convertedAmount / amount else null,
            category = if (isTransfer) Categories.TRANSFER else state.category,
            note = state.note.trim(),
            date = state.date,
            time = DateUtils.timeMinutes(state.date),
            createdAt = oldTxn?.createdAt ?: 0L,
        )

        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            val result = if (state.isEdit && oldTxn != null) {
                transactionRepository.updateTransaction(userId, oldTxn!!, newTxn)
            } else {
                transactionRepository.addTransaction(userId, newTxn)
            }
            state = when (result) {
                is Resource.Success<*> -> state.copy(loading = false, success = true)
                is Resource.Error -> state.copy(loading = false, error = result.message)
                Resource.Loading -> state
            }
        }
    }

    private fun formatAmount(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}
