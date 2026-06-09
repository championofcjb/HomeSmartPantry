package com.example.homesmartpantry.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.domain.model.InventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val inventory: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllInventory().collect { items ->
                _uiState.value = HomeUiState(
                    inventory = items,
                    isLoading = false
                )
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteInventory(id)
        }
    }

    fun updateQuantity(id: Long, newQuantity: Double) {
        viewModelScope.launch {
            if (newQuantity <= 0) {
                repository.deleteInventory(id)
            } else {
                repository.updateQuantity(id, newQuantity)
            }
        }
    }

    fun deleteItems(ids: List<Long>) {
        viewModelScope.launch {
            ids.forEach { id -> repository.deleteInventory(id) }
        }
    }
}
