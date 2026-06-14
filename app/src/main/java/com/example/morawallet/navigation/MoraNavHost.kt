package com.example.morawallet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.morawallet.feature.auth.LoginScreen
import com.example.morawallet.feature.auth.RegisterScreen
import com.example.morawallet.feature.auth.SplashScreen
import com.example.morawallet.feature.dashboard.DashboardScreen
import com.example.morawallet.feature.markets.ConverterScreen
import com.example.morawallet.feature.markets.MarketsScreen
import com.example.morawallet.feature.markets.RateChartScreen
import com.example.morawallet.feature.news.NewsDetailScreen
import com.example.morawallet.feature.news.NewsScreen
import com.example.morawallet.feature.settings.SettingsScreen
import com.example.morawallet.feature.transaction.TransactionDetailScreen
import com.example.morawallet.feature.transaction.TransactionFormScreen
import com.example.morawallet.feature.transaction.TransactionsScreen
import com.example.morawallet.feature.wallet.WalletDetailScreen
import com.example.morawallet.feature.wallet.WalletFormScreen
import com.example.morawallet.feature.wallet.WalletsScreen

/**
 * App navigation graph. Auth flow gates the tab destinations. Tab content is filled
 * in phase by phase (wallets, transactions, markets, news, dashboard).
 */
@Composable
fun MoraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val toTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onSeeAllWallets = { toTab(Routes.WALLETS) },
                onWalletClick = { id -> navController.navigate(Routes.walletDetail(id)) },
                onAddWallet = { navController.navigate(Routes.walletForm()) },
                onSeeAllTransactions = { toTab(Routes.TRANSACTIONS) },
                onTransactionClick = { id -> navController.navigate(Routes.txnDetail(id)) },
                onSeeAllMarkets = { toTab(Routes.MARKETS) },
                onAddTransaction = { type -> navController.navigate(Routes.txnForm(type = type)) },
            )
        }

        composable(Routes.WALLETS) {
            WalletsScreen(
                onWalletClick = { id -> navController.navigate(Routes.walletDetail(id)) },
                onAddWallet = { navController.navigate(Routes.walletForm()) },
            )
        }
        composable(
            route = Routes.WALLET_FORM,
            arguments = listOf(
                navArgument("walletId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            WalletFormScreen(
                walletId = entry.arguments?.getString("walletId"),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.WALLET_DETAIL,
            arguments = listOf(navArgument("walletId") { type = NavType.StringType }),
        ) { entry ->
            WalletDetailScreen(
                walletId = entry.arguments?.getString("walletId").orEmpty(),
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.walletForm(id)) },
                onTransactionClick = { id -> navController.navigate(Routes.txnDetail(id)) },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable(Routes.TRANSACTIONS) {
            TransactionsScreen(
                onTransactionClick = { id -> navController.navigate(Routes.txnDetail(id)) },
            )
        }
        composable(
            route = Routes.TXN_FORM,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType; defaultValue = "EXPENSE" },
                navArgument("txnId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            TransactionFormScreen(
                type = entry.arguments?.getString("type"),
                txnId = entry.arguments?.getString("txnId"),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.TXN_DETAIL,
            arguments = listOf(navArgument("txnId") { type = NavType.StringType }),
        ) { entry ->
            TransactionDetailScreen(
                txnId = entry.arguments?.getString("txnId").orEmpty(),
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.txnForm(txnId = id)) },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable(Routes.MARKETS) {
            MarketsScreen(
                onOpenConverter = { navController.navigate(Routes.CONVERTER) },
                onRateClick = { base, quote -> navController.navigate(Routes.rateChart(base, quote)) },
            )
        }
        composable(Routes.CONVERTER) {
            ConverterScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.RATE_CHART,
            arguments = listOf(
                navArgument("base") { type = NavType.StringType },
                navArgument("quote") { type = NavType.StringType },
            ),
        ) { entry ->
            RateChartScreen(
                base = entry.arguments?.getString("base").orEmpty(),
                quote = entry.arguments?.getString("quote").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.NEWS) {
            NewsScreen(
                onArticleClick = { id -> navController.navigate(Routes.newsDetail(id)) },
            )
        }
        composable(
            route = Routes.NEWS_DETAIL,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType }),
        ) { entry ->
            NewsDetailScreen(
                articleId = entry.arguments?.getString("articleId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
            )
        }
    }
}
