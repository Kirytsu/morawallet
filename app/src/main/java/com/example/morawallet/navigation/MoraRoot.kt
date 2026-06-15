package com.example.morawallet.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.morawallet.core.ui.components.LocalSnackbarController
import com.example.morawallet.core.ui.components.MoraSnackbarHost
import com.example.morawallet.core.ui.components.rememberSnackbarController

/** Root container: hosts the nav graph and shows the bottom bar + add FAB on tabs. */
@Composable
fun MoraRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = TopLevelDestination.entries.find { it.route == currentRoute }
    val showBars = currentDestination != null
    val snackbarController = rememberSnackbarController()

    CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
    Scaffold(
        snackbarHost = { MoraSnackbarHost(snackbarController.hostState) },
        topBar = {
            if (currentDestination != null) {
                MoraTopBar(
                    title = if (currentDestination == TopLevelDestination.DASHBOARD) {
                        "Mora Wallet"
                    } else {
                        currentDestination.label
                    },
                    showLogo = currentDestination == TopLevelDestination.DASHBOARD,
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    actions = {},
                )
            }
        },
        bottomBar = {
            if (showBars) {
                MoraBottomBar(currentRoute = currentRoute) { dest ->
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { padding ->
        MoraNavHost(navController = navController, modifier = Modifier.padding(padding))
    }
    }
}
