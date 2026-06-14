package com.example.morawallet.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MoraBottomBar(
    currentRoute: String?,
    onSelect: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { dest ->
            val accent = when (dest) {
                TopLevelDestination.DASHBOARD -> Color(0xFF1F7CFF)
                TopLevelDestination.WALLETS -> Color(0xFF24A7F2)
                TopLevelDestination.TRANSACTIONS -> Color(0xFF13A079)
                TopLevelDestination.MARKETS -> Color(0xFF8A63D2)
                TopLevelDestination.NEWS -> Color(0xFFEF7C45)
            }
            NavigationBarItem(
                selected = currentRoute == dest.route,
                onClick = { onSelect(dest) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = accent.copy(alpha = 0.14f),
                    unselectedIconColor = accent.copy(alpha = 0.58f),
                    unselectedTextColor = accent.copy(alpha = 0.74f),
                ),
            )
        }
    }
}
