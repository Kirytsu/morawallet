package com.example.morawallet.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.morawallet.data.model.TransactionType

data class Category(
    val name: String,
    val icon: ImageVector,
    val colorIndex: Int,
)

/** Predefined transaction categories per type (v1: fixed list). */
object Categories {

    const val TRANSFER = "Transfer"

    val INCOME: List<Category> = listOf(
        Category("Salary", Icons.Filled.Payments, 7),
        Category("Business", Icons.Filled.Work, 3),
        Category("Gift", Icons.Filled.CardGiftcard, 1),
        Category("Investment", Icons.AutoMirrored.Filled.TrendingUp, 6),
        Category("Other income", Icons.Filled.MoreHoriz, 11),
    )

    val EXPENSE: List<Category> = listOf(
        Category("Food", Icons.Filled.Restaurant, 0),
        Category("Transport", Icons.Filled.DirectionsCar, 4),
        Category("Shopping", Icons.Filled.ShoppingCart, 2),
        Category("Bills", Icons.Filled.Receipt, 9),
        Category("Entertainment", Icons.Filled.Movie, 10),
        Category("Health", Icons.Filled.LocalHospital, 5),
        Category("Home", Icons.Filled.Home, 8),
        Category("Education", Icons.Filled.School, 6),
        Category("Other", Icons.Filled.MoreHoriz, 11),
    )

    fun forType(type: TransactionType): List<Category> = when (type) {
        TransactionType.INCOME -> INCOME
        TransactionType.EXPENSE -> EXPENSE
        TransactionType.TRANSFER -> emptyList()
    }

    private fun find(name: String): Category? = (INCOME + EXPENSE).firstOrNull { it.name == name }

    fun icon(name: String): ImageVector =
        if (name == TRANSFER) Icons.Filled.SwapHoriz else find(name)?.icon ?: Icons.Filled.MoreHoriz

    fun colorIndex(name: String): Int =
        if (name == TRANSFER) 5 else find(name)?.colorIndex ?: 11
}
