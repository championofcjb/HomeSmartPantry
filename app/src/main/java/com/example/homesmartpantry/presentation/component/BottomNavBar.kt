package com.example.homesmartpantry.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.homesmartpantry.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.Flow

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Kitchen, NavRoutes.HOME),
    BottomNavItem("菜谱", Icons.Default.MenuBook, NavRoutes.RECIPES),
    BottomNavItem("今日做菜", Icons.Default.Restaurant, NavRoutes.TODAY_COOK),
    BottomNavItem("设置", Icons.Default.Settings, NavRoutes.SETTINGS)
)

@Composable
fun BottomNavBar(
    navController: NavController,
    todayCookCount: Flow<Int>? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val cookCount = if (todayCookCount != null) {
        todayCookCount.collectAsState(initial = 0).value
    } else {
        0
    }

    NavigationBar {
        bottomNavItems.forEach { item ->
            val showBadge = item.route == NavRoutes.TODAY_COOK && cookCount > 0
            NavigationBarItem(
                icon = {
                    if (showBadge) {
                        BadgedBox(badge = {
                            Badge { Text("$cookCount", style = androidx.compose.material3.MaterialTheme.typography.labelSmall) }
                        }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
