package com.example.homesmartpantry.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.presentation.screen.favorites.FavoritesScreen
import com.example.homesmartpantry.presentation.screen.home.HomeScreen
import com.example.homesmartpantry.presentation.screen.home.HomeViewModel
import com.example.homesmartpantry.presentation.screen.ingredient.AddIngredientScreen
import com.example.homesmartpantry.presentation.screen.ingredient.AddIngredientViewModel
import com.example.homesmartpantry.presentation.screen.ingredient.EditIngredientScreen
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
                onEditItemClick = { id -> navController.navigate(NavRoutes.editInventory(id)) },
                onAddToShoppingList = { name, qty, unit ->
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.addSingleShoppingItem(name, qty, unit)
                    }
                },
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
                onRecipeClick = { id -> navController.navigate(NavRoutes.recipeDetail(id)) },
                onFavoritesClick = { navController.navigate(NavRoutes.FAVORITES) }
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
                onEdit = { navController.navigate(NavRoutes.editRecipe(recipeId)) },
                onAddedToTodayCook = {
                    navController.popBackStack(NavRoutes.RECIPES, inclusive = false)
                }
            )
        }

        composable(NavRoutes.SHOPPING_LIST) {
            val ingredients by repository.getAllIngredients().collectAsState(initial = emptyList())

            ShoppingListScreen(
                shoppingItems = repository.getShoppingItems(),
                knownIngredients = ingredients,
                onMarkPurchased = { id ->
                    CoroutineScope(Dispatchers.IO).launch { repository.markPurchased(id) }
                },
                onMarkUnpurchased = { id ->
                    CoroutineScope(Dispatchers.IO).launch { repository.markUnpurchased(id) }
                },
                onDelete = { id ->
                    CoroutineScope(Dispatchers.IO).launch { repository.deleteShoppingItem(id) }
                },
                onClearPurchased = {
                    CoroutineScope(Dispatchers.IO).launch { repository.clearPurchased() }
                },
                onBack = { navController.popBackStack() },
                onAddItem = { name, qty, unit ->
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.addSingleShoppingItem(name, qty, unit)
                    }
                },
                onUpdateItem = { id, name, qty, unit ->
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.updateShoppingItem(id, name, qty, unit)
                    }
                },
                onAddToInventory = { name, qty, loc, unit, expireDate ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val allIngs = repository.getAllIngredients().first()
                        val existing = allIngs.find { it.name.equals(name, ignoreCase = true) }
                        val ingredientId = if (existing != null) {
                            existing.id
                        } else {
                            repository.addIngredient(name, unit, "食材")
                        }
                        repository.addInventory(
                            ingredientId = ingredientId, quantity = qty,
                            storageLocation = loc, expireDate = expireDate,
                            purchaseDate = System.currentTimeMillis()
                        )
                    }
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onShoppingListClick = { navController.navigate(NavRoutes.SHOPPING_LIST) },
                onFavoritesClick = { navController.navigate(NavRoutes.FAVORITES) },
                onTodayCookClick = {
                    navController.navigate(NavRoutes.TODAY_COOK) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onExportData = {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        val items = repository.getAllInventory().first()
                        val recipes = repository.getAllRecipes().first()
                        val shopping = repository.getShoppingItems().first()

                        val sb = StringBuilder()
                        sb.appendLine("=== HomeSmartPantry 数据导出 ===")
                        sb.appendLine("导出时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
                        sb.appendLine()

                        sb.appendLine("--- 库存 (${items.size} 项) ---")
                        items.forEach { sb.appendLine("  ${it.ingredientName} × ${it.quantity} ${it.unit} [${it.storageLocation}]") }

                        sb.appendLine()
                        sb.appendLine("--- 菜谱 (${recipes.size} 个) ---")
                        recipes.forEach { sb.appendLine("  ${it.name} (${it.category})") }

                        sb.appendLine()
                        val unpurchased = shopping.filter { !it.isPurchased }
                        sb.appendLine("--- 待采购 (${unpurchased.size} 项) ---")
                        unpurchased.forEach { sb.appendLine("  ${it.ingredientName} ${it.quantity} ${it.unit}") }

                        val ctx = navController.context
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "HomeSmartPantry 数据导出")
                        }
                        ctx.startActivity(android.content.Intent.createChooser(intent, "分享数据"))
                    }
                }
            )
        }

        composable(NavRoutes.TODAY_COOK) {
            TodayCookScreen(
                viewModel = recipeViewModel,
                onRecipeClick = { id -> navController.navigate(NavRoutes.recipeDetail(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.FAVORITES) {
            FavoritesScreen(
                favoriteRecipes = repository.getFavoriteRecipes(),
                onBack = { navController.popBackStack() },
                onRecipeClick = { id -> navController.navigate(NavRoutes.recipeDetail(id)) },
                onRemoveFavorite = { id ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        repository.setFavorite(id, false)
                    }
                }
            )
        }

        composable(
            route = NavRoutes.EDIT_INVENTORY,
            arguments = listOf(navArgument("inventoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val inventoryId = backStackEntry.arguments?.getLong("inventoryId") ?: return@composable
            val inventoryItem = remember { mutableStateOf<com.example.homesmartpantry.domain.model.InventoryItem?>(null) }

            LaunchedEffect(inventoryId) {
                inventoryItem.value = repository.getAllInventory().first()
                    .find { it.id == inventoryId }
            }

            EditIngredientScreen(
                item = inventoryItem.value,
                onBack = { navController.popBackStack() },
                onSave = { qty, loc, price, expireDate ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        if (qty <= 0) {
                            repository.deleteInventory(inventoryId)
                        } else {
                            repository.updateQuantity(inventoryId, qty)
                            val entity = repository.getInventoryById(inventoryId)?.copy(
                                storageLocation = loc,
                                price = price,
                                expireDate = expireDate
                            )
                            if (entity != null) repository.updateInventoryEntity(entity)
                        }
                    }
                },
                onDelete = {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        repository.deleteInventory(inventoryId)
                    }
                    navController.popBackStack()
                },
                onUpdateIngredient = { name, cat, unit ->
                    val ingId = inventoryItem.value?.ingredientId
                    if (ingId != null) {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            repository.updateIngredient(ingId, name, unit, cat)
                        }
                    }
                }
            )
        }
    }
}
