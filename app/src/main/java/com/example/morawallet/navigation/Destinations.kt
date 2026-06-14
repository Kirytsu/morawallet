package com.example.morawallet.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/** Route strings for every destination in the app. */
object Routes {
    // Auth
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Top-level tabs
    const val DASHBOARD = "dashboard"
    const val WALLETS = "wallets"
    const val TRANSACTIONS = "transactions"
    const val MARKETS = "markets"
    const val NEWS = "news"

    // Secondary
    const val SETTINGS = "settings"
    const val CONVERTER = "converter"

    // Parameterized
    const val WALLET_DETAIL = "wallet/{walletId}"
    fun walletDetail(walletId: String) = "wallet/$walletId"

    const val WALLET_FORM = "wallet_form?walletId={walletId}"
    fun walletForm(walletId: String? = null) =
        if (walletId == null) "wallet_form" else "wallet_form?walletId=$walletId"

    const val TXN_FORM = "txn_form?type={type}&txnId={txnId}"
    fun txnForm(type: String? = null, txnId: String? = null): String {
        val t = type ?: "EXPENSE"
        return if (txnId == null) "txn_form?type=$t" else "txn_form?type=$t&txnId=$txnId"
    }

    const val TXN_DETAIL = "txn/{txnId}"
    fun txnDetail(txnId: String) = "txn/$txnId"

    const val RATE_CHART = "rate/{base}/{quote}"
    fun rateChart(base: String, quote: String) = "rate/$base/$quote"

    const val NEWS_DETAIL = "news_detail/{articleId}"
    fun newsDetail(articleId: String) = "news_detail/$articleId"
}

/** Bottom-navigation tabs. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    WALLETS(Routes.WALLETS, "Wallets", Icons.Filled.AccountBalanceWallet),
    TRANSACTIONS(Routes.TRANSACTIONS, "Records", Icons.AutoMirrored.Filled.ReceiptLong),
    MARKETS(Routes.MARKETS, "Markets", Icons.AutoMirrored.Filled.ShowChart),
    NEWS(Routes.NEWS, "News", Icons.AutoMirrored.Filled.Article),
}
