package com.example.homesmartpantry.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.homesmartpantry.data.local.dao.IngredientDao
import com.example.homesmartpantry.data.local.dao.InventoryDao
import com.example.homesmartpantry.data.local.dao.InventoryEventDao
import com.example.homesmartpantry.data.local.dao.RecipeDao
import com.example.homesmartpantry.data.local.dao.ShoppingDao
import com.example.homesmartpantry.data.local.dao.TodayCookDao
import com.example.homesmartpantry.data.local.entity.IngredientEntity
import com.example.homesmartpantry.data.local.entity.InventoryEntity
import com.example.homesmartpantry.data.local.entity.InventoryEventEntity
import com.example.homesmartpantry.data.local.entity.RecipeEntity
import com.example.homesmartpantry.data.local.entity.RecipeIngredientEntity
import com.example.homesmartpantry.data.local.entity.RecipeStepEntity
import com.example.homesmartpantry.data.local.entity.RecipeTagEntity
import com.example.homesmartpantry.data.local.entity.ShoppingItemEntity
import com.example.homesmartpantry.data.local.entity.TodayCookEntity

@Database(
    entities = [
        IngredientEntity::class,
        InventoryEntity::class,
        InventoryEventEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        RecipeStepEntity::class,
        RecipeTagEntity::class,
        ShoppingItemEntity::class,
        TodayCookEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun inventoryEventDao(): InventoryEventDao
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun todayCookDao(): TodayCookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `today_cook_list` (`recipeId` INTEGER NOT NULL, `addedDate` INTEGER NOT NULL, PRIMARY KEY(`recipeId`), FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_today_cook_list_recipeId` ON `today_cook_list` (`recipeId`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "home_smart_pantry.db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
