package com.example.morawallet

import com.example.morawallet.core.util.ReportAggregations
import com.example.morawallet.core.util.ReportRange
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportAggregationsTest {

    private val now = 1_700_000_000_000L
    private val day = 86_400_000L

    private fun txn(
        type: TransactionType,
        amount: Double,
        daysAgo: Int,
        category: String = "",
    ) = Transaction(
        id = "$type-$amount-$daysAgo",
        type = type.name,
        amount = amount,
        currencyCode = "USD",
        category = category,
        date = now - daysAgo * day,
    )

    private val txns = listOf(
        txn(TransactionType.INCOME, 100.0, daysAgo = 0),
        txn(TransactionType.EXPENSE, 40.0, daysAgo = 0, category = "Food"),
        txn(TransactionType.EXPENSE, 60.0, daysAgo = 2, category = "Transport"),
        txn(TransactionType.INCOME, 200.0, daysAgo = 10), // outside the week window
        txn(TransactionType.TRANSFER, 500.0, daysAgo = 0), // never counted
    )

    @Test
    fun week_series_sums_income_and_expense_in_window() {
        val series = ReportAggregations.incomeExpenseSeries(txns, ReportRange.WEEK, now)
        assertEquals(100.0, series.sumOf { it.income }, 0.001)
        assertEquals(100.0, series.sumOf { it.expense }, 0.001)
    }

    @Test
    fun category_breakdown_sorted_descending_expenses_only() {
        val breakdown = ReportAggregations.categoryBreakdown(txns, ReportRange.WEEK, now)
        assertEquals(2, breakdown.size)
        assertEquals("Transport", breakdown[0].category)
        assertEquals(60.0, breakdown[0].amount, 0.001)
        assertEquals("Food", breakdown[1].category)
    }

    @Test
    fun category_breakdown_can_report_income() {
        val breakdown = ReportAggregations.categoryBreakdown(
            txns = txns,
            range = ReportRange.WEEK,
            now = now,
            type = TransactionType.INCOME,
        )
        assertEquals(1, breakdown.size)
        assertEquals("Other", breakdown[0].category)
        assertEquals(100.0, breakdown[0].amount, 0.001)
    }

    @Test
    fun transfers_are_excluded_from_reports() {
        val series = ReportAggregations.incomeExpenseSeries(txns, ReportRange.WEEK, now)
        // 500 transfer must not appear in either income or expense totals.
        assertEquals(200.0, series.sumOf { it.income + it.expense }, 0.001)
    }

    @Test
    fun to_base_mapper_converts_amounts() {
        val eurTxn = listOf(
            Transaction(
                id = "eur",
                type = TransactionType.EXPENSE.name,
                amount = 100.0,
                currencyCode = "EUR",
                category = "Food",
                date = now,
            ),
        )
        // 1 base = 0.5 EUR, so 100 EUR -> 200 base.
        val breakdown = ReportAggregations.categoryBreakdown(eurTxn, ReportRange.WEEK, now) { amount, code ->
            if (code == "EUR") amount / 0.5 else amount
        }
        assertEquals(200.0, breakdown[0].amount, 0.001)
    }
}
