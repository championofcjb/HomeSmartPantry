package com.example.homesmartpantry.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.domain.model.InventoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CookableInfo(
    val fullCount: Int = 0,
    val partialCount: Int = 0,
    val topRecipes: List<String> = emptyList()
)

data class HomeUiState(
    val inventory: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val cookable: CookableInfo = CookableInfo(),
    val shoppingCount: Int = 0
)

class HomeViewModel(
    private val repository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllInventory(),
                repository.getAllRecipes(),
                repository.getUnpurchasedCount()
            ) { items, recipes, count ->
                Triple(items, recipes, count)
            }.collect { (items, recipes, count) ->
                val cookable = withContext(Dispatchers.IO) {
                    var full = 0; var partial = 0; val topNames = mutableListOf<String>()
                    for (recipe in recipes) {
                        val ingredients = repository.getRecipeIngredientsOnce(recipe.id)
                        if (ingredients.isEmpty()) continue
                        val matched = ingredients.count { ing ->
                            items.any { inv -> inv.ingredientName.contains(ing.ingredientName, ignoreCase = true) && inv.quantity > 0 }
                        }
                        when { matched == ingredients.size -> { full++; if (topNames.size < 3) topNames.add(recipe.name) }
                            matched >= ingredients.size / 2 -> partial++ }
                    }
                    CookableInfo(full, partial, topNames)
                }
                _uiState.value = HomeUiState(inventory = items, isLoading = false, cookable = cookable, shoppingCount = count)
            }
        }
    }

    fun deleteItem(id: Long) { viewModelScope.launch { repository.deleteInventory(id) } }

    fun updateQuantity(id: Long, newQuantity: Double) {
        viewModelScope.launch {
            if (newQuantity <= 0) repository.deleteInventory(id)
            else repository.updateQuantity(id, newQuantity)
        }
    }

    fun deleteItems(ids: List<Long>) {
        viewModelScope.launch { ids.forEach { repository.deleteInventory(it) } }
    }

    fun updateItem(id: Long, quantity: Double, storageLocation: String, price: Double?) {
        viewModelScope.launch {
            if (quantity <= 0) repository.deleteInventory(id)
            else repository.updateQuantity(id, quantity)
            // Storage location and price require direct DAO access
            val entity = repository.getInventoryById(id)?.copy(
                storageLocation = storageLocation,
                price = price
            )
            if (entity != null) repository.updateInventoryEntity(entity)
        }
    }
}
