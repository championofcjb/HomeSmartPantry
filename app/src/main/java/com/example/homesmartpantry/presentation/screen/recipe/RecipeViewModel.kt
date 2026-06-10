package com.example.homesmartpantry.presentation.screen.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.data.local.dao.TodayCookWithRecipe
import com.example.homesmartpantry.domain.model.Ingredient
import com.example.homesmartpantry.domain.model.InventoryItem
import com.example.homesmartpantry.domain.model.Recipe
import com.example.homesmartpantry.domain.model.RecipeIngredient
import com.example.homesmartpantry.domain.model.RecipeStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── List state ──
enum class Availability { FULL, PARTIAL, NONE }

data class RecipeWithStatus(
    val recipe: Recipe,
    val availability: Availability,
    val matchCount: Int,
    val totalCount: Int,
    val missingIngredients: List<String>
)

enum class RecipeSortOption { NAME, RATING, NEWEST }

data class RecipeUiState(
    val recipes: List<RecipeWithStatus> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val sortOption: RecipeSortOption = RecipeSortOption.NAME
)

// ── Detail state ──
enum class IngredientStockStatus { SUFFICIENT, INSUFFICIENT, MISSING }

data class IngredientWithStock(
    val name: String,
    val requiredQty: String,
    val unit: String,
    val stockQty: String = "",
    val stockUnit: String = "",
    val status: IngredientStockStatus = IngredientStockStatus.MISSING
)

data class RecipeDetailState(
    val recipe: Recipe? = null,
    val ingredients: List<IngredientWithStock> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val tags: List<String> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = true
)

class RecipeViewModel(
    private val repository: IngredientRepository
) : ViewModel() {

    // ── List state ──
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // ── Detail state ──
    private val _detailState = MutableStateFlow(RecipeDetailState())
    val detailState: StateFlow<RecipeDetailState> = _detailState.asStateFlow()

    private var allInventory: List<InventoryItem> = emptyList()
    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                repository.getAllRecipes(),
                repository.getAllInventory(),
                _searchQuery
            ) { recipes, inventory, query ->
                allInventory = inventory
                Triple(recipes, inventory, query)
            }.collect { (recipes, inventory, query) ->
                val filtered = if (query.isBlank()) recipes
                else recipes.filter {
                    val q = query.lowercase()
                    it.name.contains(q, ignoreCase = true) ||
                    it.description.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true)
                }
                val withStatus = withContext(Dispatchers.IO) {
                    filtered.map { recipe ->
                        calculateAvailability(recipe.id, recipe.name, inventory)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    recipes = applyFilter(withStatus, _uiState.value.selectedTab),
                    searchQuery = query
                )
            }
        }
    }

    // ── List methods ──

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private suspend fun calculateAvailability(
        recipeId: Long, recipeName: String, inventory: List<InventoryItem>
    ): RecipeWithStatus {
        val ingredients = repository.getRecipeIngredientsOnce(recipeId)
        val totalCount = ingredients.size
        if (totalCount == 0) return RecipeWithStatus(
            Recipe(recipeId, recipeName), Availability.NONE, 0, 0, emptyList()
        )
        val matched = ingredients.filter { ing ->
            inventory.any { invItem ->
                invItem.ingredientName.contains(ing.ingredientName, ignoreCase = true) && invItem.quantity > 0
            }
        }
        val missing = ingredients.filterNot { it in matched.toSet() }
        val availability = when {
            matched.size == totalCount -> Availability.FULL
            matched.size >= totalCount / 2 -> Availability.PARTIAL
            else -> Availability.NONE
        }
        return RecipeWithStatus(
            Recipe(recipeId, recipeName), availability, matched.size, totalCount,
            missing.map { it.ingredientName }
        )
    }

    private fun applyFilter(recipes: List<RecipeWithStatus>, tab: Int): List<RecipeWithStatus> {
        val availabilitySorted = recipes.sortedBy { when (it.availability) {
            Availability.FULL -> 0; Availability.PARTIAL -> 1; Availability.NONE -> 2
        } }
        val tabFiltered = when (tab) {
            0 -> availabilitySorted
            1 -> availabilitySorted.filter { it.availability == Availability.FULL }
            2 -> availabilitySorted.filter { it.availability == Availability.PARTIAL }
            3 -> availabilitySorted.filter { it.recipe.isFavorite }
            else -> availabilitySorted
        }
        return when (_uiState.value.sortOption) {
            RecipeSortOption.NAME -> tabFiltered.sortedBy { it.recipe.name.lowercase() }
            RecipeSortOption.RATING -> tabFiltered.sortedByDescending { it.recipe.rating }
            RecipeSortOption.NEWEST -> tabFiltered.sortedByDescending { it.recipe.createDate }
        }
    }

    fun setTab(tab: Int) {
        viewModelScope.launch {
            val fullList = calculateAllStatuses()
            _uiState.value = _uiState.value.copy(selectedTab = tab, recipes = applyFilter(fullList, tab))
        }
    }

    fun setSortOption(option: RecipeSortOption) {
        val current = _uiState.value
        viewModelScope.launch {
            val fullList = calculateAllStatuses()
            _uiState.value = current.copy(sortOption = option, recipes = applyFilter(fullList, current.selectedTab))
        }
    }

    private suspend fun calculateAllStatuses(): List<RecipeWithStatus> {
        return _uiState.value.recipes.map { calculateAvailability(it.recipe.id, it.recipe.name, allInventory) }
    }

    fun deleteRecipe(id: Long) { viewModelScope.launch { repository.deleteRecipe(id) } }

    // ── Edit methods ──

    data class RecipeEditData(
        val recipe: com.example.homesmartpantry.domain.model.Recipe,
        val ingredients: List<RecipeIngredient>,
        val steps: List<RecipeStep>,
        val tags: List<String>
    )

    private val _editData = MutableStateFlow<RecipeEditData?>(null)
    val editData: StateFlow<RecipeEditData?> = _editData.asStateFlow()

    fun loadEditData(recipeId: Long) {
        viewModelScope.launch {
            val recipe = repository.getRecipeById(recipeId) ?: return@launch
            val ingredients = repository.getRecipeIngredientsOnce(recipeId)
            val steps = repository.getRecipeStepsOnce(recipeId)
            val tags = repository.getRecipeTagsOnce(recipeId)
            _editData.value = RecipeEditData(recipe, ingredients, steps, tags)
        }
    }

    fun saveRecipe(
        id: Long?, name: String, description: String,
        category: String = "家常菜", difficulty: String = "普通", cookTime: String = "30分钟",
        servings: String = "2人份", calories: String = "", protein: String = "", fat: String = "",
        notes: String = "",
        ingredients: List<RecipeIngredient> = emptyList(),
        steps: List<RecipeStep> = emptyList(), tags: List<String> = emptyList(),
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveRecipe(id, name, description, null, category, difficulty, cookTime,
                servings, calories, protein, fat, notes, ingredients, steps, tags)
            onDone()
        }
    }

    // ── Detail methods ──

    fun loadDetail(recipeId: Long) {
        viewModelScope.launch {
            _detailState.value = RecipeDetailState(isLoading = true)
            val recipe = repository.getRecipeById(recipeId)
            val ingredients = repository.getRecipeIngredientsOnce(recipeId)
            val steps = repository.getRecipeStepsOnce(recipeId)
            val tags = repository.getRecipeTagsOnce(recipeId)
            val inventory = allInventory.ifEmpty {
                repository.getAllInventory().first()
            }

            val matchedIngredients = ingredients.map { ing ->
                val stock = inventory.filter { invItem ->
                    invItem.ingredientName.contains(ing.ingredientName, ignoreCase = true) && invItem.quantity > 0
                }
                val totalStock = stock.sumOf { it.quantity }
                val required = ing.quantity.toDoubleOrNull()
                val status = when {
                    stock.isEmpty() -> IngredientStockStatus.MISSING
                    required != null && totalStock < required -> IngredientStockStatus.INSUFFICIENT
                    else -> IngredientStockStatus.SUFFICIENT
                }
                IngredientWithStock(
                    name = ing.ingredientName, requiredQty = ing.quantity, unit = ing.unit,
                    stockQty = if (stock.isNotEmpty()) "${formatQty(totalStock)}" else "",
                    stockUnit = stock.firstOrNull()?.unit ?: ing.unit,
                    status = status
                )
            }

            _detailState.value = RecipeDetailState(
                recipe = recipe, ingredients = matchedIngredients,
                steps = steps, tags = tags, inventory = inventory, isLoading = false
            )
        }
    }

    fun toggleFavorite() {
        val recipe = _detailState.value.recipe ?: return
        viewModelScope.launch {
            repository.setFavorite(recipe.id, !recipe.isFavorite)
            _detailState.value = _detailState.value.copy(
                recipe = recipe.copy(isFavorite = !recipe.isFavorite)
            )
        }
    }

    fun setRating(rating: Float) {
        val recipe = _detailState.value.recipe ?: return
        viewModelScope.launch {
            repository.setRating(recipe.id, rating)
            _detailState.value = _detailState.value.copy(
                recipe = recipe.copy(rating = rating)
            )
        }
    }

    fun addMissingToShoppingList() {
        val missing = _detailState.value.ingredients.filter {
            it.status != IngredientStockStatus.SUFFICIENT
        }
        if (missing.isEmpty()) return
        viewModelScope.launch {
            repository.addToShoppingList(missing.map {
                com.example.homesmartpantry.domain.model.ShoppingItem(
                    ingredientName = it.name, quantity = it.requiredQty, unit = it.unit
                )
            })
        }
    }

    /**
     * 今日做菜：库存充足 → 提示可直接制作；库存不足 → 把所有食材加入购物车
     */
    sealed class CookTodayResult {
        data object CanCook : CookTodayResult()
        data class AddToCart(val count: Int) : CookTodayResult()
    }

    fun cookToday(onResult: (CookTodayResult) -> Unit) {
        val all = _detailState.value.ingredients
        val sufficient = all.none { it.status != IngredientStockStatus.SUFFICIENT }
        if (sufficient) {
            onResult(CookTodayResult.CanCook)
            return
        }
        viewModelScope.launch {
            repository.addToShoppingList(all.map {
                com.example.homesmartpantry.domain.model.ShoppingItem(
                    ingredientName = it.name, quantity = it.requiredQty, unit = it.unit
                )
            })
            onResult(CookTodayResult.AddToCart(all.size))
        }
    }

    private fun formatQty(qty: Double): String {
        return if (qty == qty.toLong().toDouble()) qty.toLong().toString()
        else String.format("%.1f", qty)
    }

    // ── Known ingredients for recipe form ──

    data class KnownIngredient(
        val name: String,
        val unit: String,
        val category: String,
        val stockQty: Double? = null,
        val stockUnit: String = ""
    )

    private val _knownIngredients = MutableStateFlow<List<KnownIngredient>>(emptyList())
    val knownIngredients: StateFlow<List<KnownIngredient>> = _knownIngredients.asStateFlow()

    fun loadKnownIngredients() {
        viewModelScope.launch {
            val dbIngredients = repository.getAllIngredients().first()
            val inventory = repository.getAllInventory().first()
            val result = dbIngredients.map { ing ->
                val stock = inventory.filter { inv ->
                    inv.ingredientName.contains(ing.name, ignoreCase = true) && inv.quantity > 0
                }
                val totalStock = stock.sumOf { it.quantity }
                KnownIngredient(
                    name = ing.name, unit = ing.unit, category = ing.category,
                    stockQty = if (stock.isNotEmpty()) totalStock else null,
                    stockUnit = stock.firstOrNull()?.unit ?: ing.unit
                )
            }
            _knownIngredients.value = result
        }
    }

    // ── Today cook list ──

    private val _todayCookList = MutableStateFlow<List<TodayCookWithRecipe>>(emptyList())
    val todayCookList: StateFlow<List<TodayCookWithRecipe>> = _todayCookList.asStateFlow()

    private val _todayCookCount = MutableStateFlow(0)
    val todayCookCount: StateFlow<Int> = _todayCookCount.asStateFlow()

    private val _isInTodayCook = MutableStateFlow(false)
    val isInTodayCook: StateFlow<Boolean> = _isInTodayCook.asStateFlow()

    init {
        viewModelScope.launch {
            // 自动清理昨日及更早的记录
            val todayStart = getTodayStartMillis()
            repository.clearOldTodayCookEntries(todayStart)

            // 监听今日做菜列表变化
            repository.getTodayCookList().collect { list ->
                _todayCookList.value = list
                _todayCookCount.value = list.size
            }
        }
    }

    fun addToTodayCook(recipeId: Long) {
        viewModelScope.launch {
            repository.addToTodayCook(recipeId)
        }
    }

    fun removeFromTodayCook(recipeId: Long) {
        viewModelScope.launch {
            repository.removeFromTodayCook(recipeId)
        }
    }

    fun toggleTodayCook(recipeId: Long) {
        viewModelScope.launch {
            if (repository.isInTodayCook(recipeId)) {
                repository.removeFromTodayCook(recipeId)
                _isInTodayCook.value = false
            } else {
                repository.addToTodayCook(recipeId)
                _isInTodayCook.value = true
            }
        }
    }

    fun checkTodayCookStatus(recipeId: Long) {
        viewModelScope.launch {
            _isInTodayCook.value = repository.isInTodayCook(recipeId)
        }
    }

    fun loadTodayCookDetail(recipeId: Long) {
        viewModelScope.launch {
            val isIn = repository.isInTodayCook(recipeId)
            _isInTodayCook.value = isIn
        }
    }

    private fun getTodayStartMillis(): Long {
        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.HOUR_OF_DAY, 0)
        now.set(java.util.Calendar.MINUTE, 0)
        now.set(java.util.Calendar.SECOND, 0)
        now.set(java.util.Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }
}
