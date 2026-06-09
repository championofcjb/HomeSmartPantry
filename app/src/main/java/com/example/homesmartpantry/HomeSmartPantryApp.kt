package com.example.homesmartpantry

import android.app.Application
import android.os.Build
import com.example.homesmartpantry.data.local.AppDatabase
import com.example.homesmartpantry.data.repository.IngredientRepository
import com.example.homesmartpantry.presentation.notification.ExpiryAlarmReceiver
import com.example.homesmartpantry.presentation.notification.NotificationHelper
import com.example.homesmartpantry.presentation.screen.home.HomeViewModel
import com.example.homesmartpantry.presentation.screen.ingredient.AddIngredientViewModel
import com.example.homesmartpantry.presentation.screen.recipe.RecipeViewModel

class HomeSmartPantryApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: IngredientRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize database and repository
        database = AppDatabase.getInstance(this)
        repository = IngredientRepository(
            ingredientDao = database.ingredientDao(),
            inventoryDao = database.inventoryDao(),
            eventDao = database.inventoryEventDao(),
            recipeDao = database.recipeDao(),
            shoppingDao = database.shoppingDao(),
            todayCookDao = database.todayCookDao()
        )

        // Setup notification channel and schedule alarms
        NotificationHelper.createNotificationChannel(this)
        ExpiryAlarmReceiver.scheduleDailyChecks(this)
    }

    fun createHomeViewModel(): HomeViewModel {
        return HomeViewModel(repository)
    }

    fun createAddIngredientViewModel(): AddIngredientViewModel {
        return AddIngredientViewModel(repository)
    }

    fun createRecipeViewModel(): RecipeViewModel {
        return RecipeViewModel(repository)
    }
}
