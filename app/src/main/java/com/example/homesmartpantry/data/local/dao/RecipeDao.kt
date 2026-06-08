package com.example.homesmartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homesmartpantry.data.local.entity.RecipeEntity
import com.example.homesmartpantry.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Delete
    suspend fun delete(recipe: RecipeEntity)

    // Recipe ingredients
    @Query("""
        SELECT recipe_ingredients.*, ingredients.name AS ingredientName, ingredients.unit 
        FROM recipe_ingredients 
        INNER JOIN ingredients ON recipe_ingredients.ingredientId = ingredients.id 
        WHERE recipe_ingredients.recipeId = :recipeId
    """)
    fun getRecipeIngredients(recipeId: Long): Flow<List<RecipeIngredientWithInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredientEntity)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: Long)
}

data class RecipeIngredientWithInfo(
    val recipeId: Long,
    val ingredientId: Long,
    val requiredQty: Double,
    val ingredientName: String,
    val unit: String
)
