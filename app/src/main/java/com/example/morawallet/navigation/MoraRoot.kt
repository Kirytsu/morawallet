package com.example.morawallet.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                    actions = {
                        if (currentDestination == TopLevelDestination.WALLETS) {
                            Button(onClick = { navController.navigate(Routes.walletForm()) }) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Add wallet")
                            }
                        }
                    },
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
