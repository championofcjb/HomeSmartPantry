package com.example.homesmartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unit: String, // g, ml, 个, 瓶, 袋...
    val category: String = "食材", // 食材, 调味料, 主食粮油
    val imageUri: String? = null
)
