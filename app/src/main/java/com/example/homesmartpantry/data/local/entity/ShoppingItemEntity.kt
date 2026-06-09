package com.example.homesmartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ingredientName: String,
    val quantity: String = "",
    val unit: String = "",
    val isPurchased: Boolean = false,
    val sourceRecipeId: Long? = null,  // 来自哪个菜谱
    val createDate: Long = System.currentTimeMillis()
)
