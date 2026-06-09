package com.example.homesmartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homesmartpantry.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_list ORDER BY isPurchased ASC, createDate DESC")
    fun getAllItems(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_list WHERE isPurchased = 0 ORDER BY createDate DESC")
    fun getUnpurchasedItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_list SET isPurchased = 1 WHERE id = :id")
    suspend fun markPurchased(id: Long)

    @Query("UPDATE shopping_list SET isPurchased = 0 WHERE id = :id")
    suspend fun markUnpurchased(id: Long)

    @Query("DELETE FROM shopping_list WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM shopping_list WHERE isPurchased = 1")
    suspend fun clearPurchased()

    @Query("SELECT COUNT(*) FROM shopping_list WHERE isPurchased = 0")
    fun getUnpurchasedCount(): Flow<Int>
}
