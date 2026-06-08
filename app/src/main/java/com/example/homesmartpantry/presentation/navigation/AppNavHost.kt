package com.example.homesmartpantry.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.homesmartpantry.presentation.screen.home.HomeScreen
import com.example.homesmartpantry.presentation.screen.home.HomeViewModel
import com.example.homesmartpantry.presentation.screen.ingredient.AddIngredientScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onAddClick = {
                    navController.navigate(NavRoutes.ADD_INGREDIENT)
                }
            )
        }

        composable(NavRoutes.ADD_INGREDIENT) {
            AddIngredientScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Placeholder routes for future screens
        composable(NavRoutes.RECIPES) {
            androidx.compose.material3.Text("菜谱功能开发中...")
        }

        composable(NavRoutes.SETTINGS) {
            androidx.compose.material3.Text("设置功能开发中...")
        }
    }
}
