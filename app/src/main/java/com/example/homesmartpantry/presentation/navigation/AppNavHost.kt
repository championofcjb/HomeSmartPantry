package com.example.homesmartpantry.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.presentation.screen.home.HomeScreen
import com.example.homesmartpantry.presentation.screen.home.HomeViewModel
import com.example.homesmartpantry.presentation.screen.ingredient.AddIngredientScreen
import com.example.homesmartpantry.presentation.screen.ingredient.AddIngredientViewModel
import com.example.homesmartpantry.presentation.screen.recipe.AddEditRecipeScreen
import com.example.homesmartpantry.presentation.screen.recipe.RecipeDetailScreen
import com.example.homesmartpantry.presentation.screen.recipe.RecipeListScreen
import com.example.homesmartpantry.presentation.screen.recipe.RecipeViewModel
import com.example.homesmartpantry.presentation.screen.settings.SettingsScreen
import com.example.homesmartpantry.presentation.screen.shopping.ShoppingListScreen
import com.example.homesmartpantry.presentation.screen.todaycook.TodayCookScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    addIngredientViewModel: AddIngredientViewModel,
    recipeViewModel: RecipeViewModel,
    repository: IngredientRepository,
    modifier: Modifier = Modifier,
    onSelectionModeChanged: (Boolean) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onAddClick = { navController.navigate(NavRoutes.ADD_INGREDIENT) },
                onRecipesClick = {
                    navController.navigate(NavRoutes.RECIPES) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onShoppingListClick = { navController.navigate(NavRoutes.SHOPPING_LIST) },
                onSelectionModeChanged = onSelectionModeChanged
            )
        }

        composable(NavRoutes.ADD_INGREDIENT) {
            AddIngredientScreen(
                viewModel = addIngredientViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.RECIPES) {
            RecipeListScreen(
                viewModel = recipeViewModel,
                onAddClick = { navController.navigate(NavRoutes.ADD_RECIPE) },
                onRecipeClick = { id -> navController.navigate(NavRoutes.recipeDetail(id)) }
            )
        }

        composable(NavRoutes.ADD_RECIPE) {
            AddEditRecipeScreen(
                viewModel = recipeViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EDIT_RECIPE,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            AddEditRecipeScreen(
                viewModel = recipeViewModel,
                editRecipeId = recipeId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.RECIPE_DETAIL,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId = recipeId,
                viewModel = recipeViewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(NavRoutes.editRecipe(recipeId)) }
            )
        }

        composable(NavRoutes.SHOPPING_LIST) {
            ShoppingListScreen(
                shoppingItems = repository.getShoppingItems(),
                onMarkPurchased = { id ->
                    CoroutineScope(Dispatchers.IO).launch { repository.markPurchased(id) }
                },
                onDelete = { id ->
                    CoroutineScope(Dispatchers.IO).launch { repository.deleteShoppingItem(id) }
                },
                onClearPurchased = {
                    CoroutineScope(Dispatchers.IO).launch { repository.clearPurchased() }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onShoppingListClick = { navController.navigate(NavRoutes.SHOPPING_LIST) }
            )
        }

        composable(NavRoutes.TODAY_COOK) {
            TodayCookScreen(
                viewModel = recipeViewModel,
                onRecipeClick = { id -> navController.navigate(NavRoutes.recipeDetail(id)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
