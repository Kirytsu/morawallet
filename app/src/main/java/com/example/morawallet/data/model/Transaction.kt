package com.example.morawallet.data.model

enum class TransactionType { INCOME, EXPENSE, TRANSFER }

/**
 * A money record. Stored at users/{uid}/transactions/{id}.
 * [amount] is always a positive magnitude in the source wallet's currency.
 * For transfers, [toWalletId]/[convertedAmount]/[fxRate] describe the destination.
 */
data class Transaction(
    val id: String = "",
    val type: String = TransactionType.EXPENSE.name,
    val amount: Double = 0.0,
    val currencyCode: String = "",
    val walletId: String = "",
    val toWalletId: String? = null,
    val toCurrencyCode: String? = null,
    val fxRate: Double? = null,
    val convertedAmount: Double? = null,
    val category: String = "",
    val note: String = "",
    val date: Long = 0L,
    /** Minutes after midnight for the record's local transaction time. */
    val time: Int = 0,
    val createdAt: Long = 0L,
) {
    val typeEnum: TransactionType
        get() = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE)
}
