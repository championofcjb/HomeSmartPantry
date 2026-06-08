package com.example.homesmartpantry

import android.app.Application
import com.example.homesmartpantry.data.local.AppDatabase
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.presentation.screen.home.HomeViewModel

class HomeSmartPantryApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: IngredientRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = IngredientRepository(
            ingredientDao = database.ingredientDao(),
            inventoryDao = database.inventoryDao(),
            eventDao = database.inventoryEventDao(),
            recipeDao = database.recipeDao()
        )
    }

    fun createHomeViewModel(): HomeViewModel {
        return HomeViewModel(repository)
    }
}
