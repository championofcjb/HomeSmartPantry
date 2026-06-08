package com.example.homesmartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_events")
data class InventoryEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val familyId: String = "default",
    val type: String, // ADD, USE, UPDATE
    val ingredientId: Long,
    val quantity: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val synced: Boolean = false
)
