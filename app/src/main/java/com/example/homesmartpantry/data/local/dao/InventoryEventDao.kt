package com.example.homesmartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homesmartpantry.data.local.entity.InventoryEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryEventDao {
    @Query("SELECT * FROM inventory_events WHERE synced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedEvents(): Flow<List<InventoryEventEntity>>

    @Query("SELECT * FROM inventory_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<InventoryEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: InventoryEventEntity): Long

    @Query("UPDATE inventory_events SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE inventory_events SET synced = 1 WHERE id IN (:ids)")
    suspend fun markMultipleAsSynced(ids: List<Long>)

    @Query("DELETE FROM inventory_events WHERE synced = 1")
    suspend fun deleteSyncedEvents()
}
