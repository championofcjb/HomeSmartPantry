package com.example.homesmartpantry.presentation.screen.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.domain.model.Ingredient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddIngredientUiState(
    val name: String = "",
    val category: String = "食材",
    val unit: String = "个",
    val quantity: String = "",
    val shelfLifeDays: String = "",
    val storageLocation: String = "冰箱冷藏",
    val price: String = "",
    val searchResults: List<Ingredient> = emptyList(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val selectedIngredient: Ingredient? = null,
    val isNewIngredient: Boolean = true,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddIngredientViewModel(
    private val repository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddIngredientUiState())
    val uiState: StateFlow<AddIngredientUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allIngredients: List<Ingredient> = emptyList()

    init {
        viewModelScope.launch {
            repository.getAllIngredients().collect { ingredients ->
                allIngredients = ingredients
                val currentName = _uiState.value.name
                if (currentName.isNotBlank()) {
                    val filtered = ingredients.filter {
                        it.name.contains(currentName, ignoreCase = true)
                    }
                    _uiState.value = _uiState.value.copy(searchResults = filtered)
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            selectedIngredient = null,
            isNewIngredient = true,
            errorMessage = null
        )
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), unit = "个")
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val filtered = allIngredients.filter {
                it.name.contains(name, ignoreCase = true)
            }
            _uiState.value = _uiState.value.copy(searchResults = filtered)
        }
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun selectIngredient(ingredient: Ingredient) {
        _uiState.value = _uiState.value.copy(
            name = ingredient.name,
            unit = ingredient.unit,
            category = ingredient.category,
            selectedIngredient = ingredient,
            isNewIngredient = false,
            searchResults = emptyList(),
            errorMessage = null
        )
    }

    fun dismissSearchResults() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun updateUnit(unit: String) {
        _uiState.value = _uiState.value.copy(unit = unit)
    }

    fun updateQuantity(quantity: String) {
        if (quantity.isEmpty() || quantity.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(quantity = quantity, errorMessage = null)
        }
    }

    fun updateShelfLifeDays(days: String) {
        if (days.isEmpty() || days.matches(Regex("^\\d*$"))) {
            _uiState.value = _uiState.value.copy(shelfLifeDays = days)
        }
    }

    fun updateStorageLocation(location: String) {
        _uiState.value = _uiState.value.copy(storageLocation = location)
    }

    fun updatePrice(price: String) {
        if (price.isEmpty() || price.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(price = price)
        }
    }

    fun save() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入食材名称")
            return
        }
        if (state.unit.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入单位")
            return
        }
        val quantityValue = state.quantity.toDoubleOrNull()
        if (quantityValue == null || quantityValue <= 0) {
            _uiState.value = state.copy(errorMessage = "请输入有效的数量")
            return
        }

        // Calculate expireDate from shelfLifeDays
        val expireDateMillis = state.shelfLifeDays.toIntOrNull()?.let { days ->
            if (days > 0) System.currentTimeMillis() + days * 24L * 60 * 60 * 1000 else null
        }
        val priceValue = state.price.toDoubleOrNull()

        _uiState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val ingredientId = if (state.selectedIngredient != null) {
                    state.selectedIngredient.id
                } else {
                    repository.addIngredient(state.name.trim(), state.unit.trim(), state.category)
                }

                repository.addInventory(
                    ingredientId = ingredientId,
                    quantity = quantityValue,
                    expireDate = expireDateMillis,
                    storageLocation = state.storageLocation,
                    purchaseDate = System.currentTimeMillis(),
                    price = priceValue
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存失败: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun reset() {
        _uiState.value = AddIngredientUiState()
    }
}
