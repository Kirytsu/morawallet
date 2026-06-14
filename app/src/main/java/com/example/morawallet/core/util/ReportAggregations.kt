package com.example.morawallet.core.util

import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Time window for dashboard reports. */
enum class ReportRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year"),
}

/** One period column for the income/expense bar chart. */
data class PeriodBar(val label: String, val income: Double, val expense: Double)

/** One slice of the income/expense category donut. */
data class CategorySlice(val category: String, val amount: Double)

private data class Bucket(val start: Long, val end: Long, val label: String)

/**
 * Pure aggregation over a user's transactions. All money is reduced to the base
 * currency via [toBase] (the VM supplies an FX-aware mapper); tests pass an identity
 * mapper. INCOME/EXPENSE are counted by [Transaction.amount] in their own currency;
 * TRANSFER is ignored (it only moves money between the user's own wallets).
 */
object ReportAggregations {

    /** Default mapper: treat amounts as already in base currency. */
    val Identity: (Double, String) -> Double = { amount, _ -> amount }

    fun startOf(range: ReportRange, now: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        when (range) {
            ReportRange.WEEK -> cal.add(Calendar.DAY_OF_YEAR, -6)
            ReportRange.MONTH -> cal.add(Calendar.DAY_OF_YEAR, -27)
            ReportRange.YEAR -> {
                cal.add(Calendar.MONTH, -11)
                cal.set(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return cal.timeInMillis
    }

    fun inRange(txns: List<Transaction>, range: ReportRange, now: Long): List<Transaction> {
        val from = startOf(range, now)
        return txns.filter { it.date in from..now }
    }

    fun incomeExpenseSeries(
        txns: List<Transaction>,
        range: ReportRange,
        now: Long,
        toBase: (Double, String) -> Double = Identity,
    ): List<PeriodBar> {
        val buckets = buckets(range, now)
        val income = DoubleArray(buckets.size)
        val expense = DoubleArray(buckets.size)
        for (txn in txns) {
            val idx = buckets.indexOfFirst { txn.date in it.start until it.end }
            if (idx < 0) continue
            val base = toBase(txn.amount, txn.currencyCode)
            when (txn.typeEnum) {
                TransactionType.INCOME -> income[idx] += base
                TransactionType.EXPENSE -> expense[idx] += base
                TransactionType.TRANSFER -> Unit
            }
        }
        return buckets.mapIndexed { i, b -> PeriodBar(b.label, income[i], expense[i]) }
    }

    fun categoryBreakdown(
        txns: List<Transaction>,
        range: ReportRange,
        now: Long,
        type: TransactionType = TransactionType.EXPENSE,
        toBase: (Double, String) -> Double = Identity,
    ): List<CategorySlice> {
        val from = startOf(range, now)
        return txns.asSequence()
            .filter { it.typeEnum == type && it.date in from..now }
            .groupBy { it.category.ifBlank { "Other" } }
            .map { (cat, list) -> CategorySlice(cat, list.sumOf { toBase(it.amount, it.currencyCode) }) }
            .filter { it.amount > 0.0 }
            .sortedByDescending { it.amount }
    }

    private fun buckets(range: ReportRange, now: Long): List<Bucket> {
        val result = mutableListOf<Bucket>()
        val cal = Calendar.getInstance().apply {
            timeInMillis = startOf(range, now)
        }
        when (range) {
            ReportRange.WEEK -> {
                val fmt = SimpleDateFormat("EEE", Locale.getDefault())
                repeat(7) {
                    val start = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    result += Bucket(start, cal.timeInMillis, fmt.format(Date(start)))
                }
            }

            ReportRange.MONTH -> {
                // Four 7-day buckets ending today.
                val fmt = SimpleDateFormat("d MMM", Locale.getDefault())
                repeat(4) {
                    val start = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 7)
                    result += Bucket(start, cal.timeInMillis, fmt.format(Date(start)))
                }
            }

            ReportRange.YEAR -> {
                val fmt = SimpleDateFormat("MMM", Locale.getDefault())
                repeat(12) {
                    val start = cal.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    result += Bucket(start, cal.timeInMillis, fmt.format(Date(start)))
                }
            }
        }
        return result
    }
}
