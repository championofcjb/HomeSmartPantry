package com.example.homesmartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.homesmartpantry.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("""
        SELECT inventory.*, ingredients.name AS ingredientName, 
               ingredients.unit, ingredients.category, ingredients.imageUri 
        FROM inventory 
        INNER JOIN ingredients ON inventory.ingredientId = ingredients.id 
        ORDER BY inventory.expireDate ASC
    """)
    fun getAllInventory(): Flow<List<InventoryWithIngredient>>

    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getById(id: Long): InventoryEntity?

    @Query("SELECT * FROM inventory WHERE ingredientId = :ingredientId")
    suspend fun getByIngredientId(ingredientId: Long): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: InventoryEntity): Long

    @Update
    suspend fun update(inventory: InventoryEntity)

    @Delete
    suspend fun delete(inventory: InventoryEntity)

    @Query("DELETE FROM inventory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM inventory WHERE expireDate IS NOT NULL AND expireDate <= :timestamp")
    suspend fun getExpiredItems(timestamp: Long): List<InventoryEntity>

    @Query("SELECT * FROM inventory WHERE expireDate IS NOT NULL AND expireDate > :now AND expireDate <= :threshold")
    suspend fun getExpiringSoon(now: Long, threshold: Long): List<InventoryEntity>
}

data class InventoryWithIngredient(
    val id: Long,
    val familyId: String,
    val ingredientId: Long,
    val quantity: Double,
    val expireDate: Long?,
    val ingredientName: String,
    val unit: String,
    val category: String,
    val storageLocation: String,
    val purchaseDate: Long?,
    val price: Double?,
    val imageUri: String?
)
