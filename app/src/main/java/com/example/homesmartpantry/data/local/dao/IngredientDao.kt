package com.example.homesmartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.homesmartpantry.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    fun searchIngredients(query: String): Flow<List<IngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: IngredientEntity): Long

    @Update
    suspend fun update(ingredient: IngredientEntity)

    @Delete
    suspend fun delete(ingredient: IngredientEntity)
}
