package com.example.homesmartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homesmartpantry.data.local.entity.RecipeEntity
import com.example.homesmartpantry.data.local.entity.RecipeIngredientEntity
import com.example.homesmartpantry.data.local.entity.RecipeStepEntity
import com.example.homesmartpantry.data.local.entity.RecipeTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    // ── Recipes ──
    @Query("SELECT * FROM recipes ORDER BY isFavorite DESC, name ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Query("""
        UPDATE recipes SET name=:name, description=:description, imageUri=:imageUri,
        category=:category, difficulty=:difficulty, cookTime=:cookTime, servings=:servings,
        calories=:calories, protein=:protein, fat=:fat, notes=:notes
        WHERE id=:id
    """)
    suspend fun update(
        id: Long, name: String, description: String, imageUri: String?,
        category: String, difficulty: String, cookTime: String, servings: String,
        calories: String, protein: String, fat: String, notes: String
    )

    @Query("UPDATE recipes SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ── Recipe Ingredients ──
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    fun getRecipeIngredients(recipeId: Long): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getRecipeIngredientsOnce(recipeId: Long): List<RecipeIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(ingredient: RecipeIngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: Long)

    // ── Recipe Steps ──
    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY stepNumber ASC")
    fun getRecipeSteps(recipeId: Long): Flow<List<RecipeStepEntity>>

    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY stepNumber ASC")
    suspend fun getRecipeStepsOnce(recipeId: Long): List<RecipeStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeStep(step: RecipeStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeSteps(steps: List<RecipeStepEntity>)

    @Query("DELETE FROM recipe_steps WHERE recipeId = :recipeId")
    suspend fun deleteRecipeSteps(recipeId: Long)

    // ── Recipe Tags ──
    @Query("SELECT tag FROM recipe_tags WHERE recipeId = :recipeId")
    fun getRecipeTags(recipeId: Long): Flow<List<String>>

    @Query("SELECT tag FROM recipe_tags WHERE recipeId = :recipeId")
    suspend fun getRecipeTagsOnce(recipeId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeTags(tags: List<RecipeTagEntity>)

    @Query("DELETE FROM recipe_tags WHERE recipeId = :recipeId")
    suspend fun deleteRecipeTags(recipeId: Long)
}
