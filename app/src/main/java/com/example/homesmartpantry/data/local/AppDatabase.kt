package com.example.homesmartpantry.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.homesmartpantry.data.local.dao.IngredientDao
import com.example.homesmartpantry.data.local.dao.InventoryDao
import com.example.homesmartpantry.data.local.dao.InventoryEventDao
import com.example.homesmartpantry.data.local.dao.RecipeDao
import com.example.homesmartpantry.data.local.entity.IngredientEntity
import com.example.homesmartpantry.data.local.entity.InventoryEntity
import com.example.homesmartpantry.data.local.entity.InventoryEventEntity
import com.example.homesmartpantry.data.local.entity.RecipeEntity
import com.example.homesmartpantry.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [
        IngredientEntity::class,
        InventoryEntity::class,
        InventoryEventEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun inventoryEventDao(): InventoryEventDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "home_smart_pantry.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
