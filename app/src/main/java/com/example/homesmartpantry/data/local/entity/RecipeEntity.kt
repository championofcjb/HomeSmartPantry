package com.example.homesmartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val imageUri: String? = null,
    val category: String = "家常菜",
    val difficulty: String = "普通",       // 简单 / 普通 / 困难
    val cookTime: String = "30分钟",       // 15分钟 / 30分钟 / 60分钟
    val servings: String = "2人份",        // 1人份 / 2人份 / 4人份
    val isFavorite: Boolean = false,
    val rating: Float = 0f,               // 0-5
    val calories: String = "",             // 热量
    val protein: String = "",              // 蛋白质
    val fat: String = "",                  // 脂肪
    val notes: String = "",                // 备注
    val createDate: Long = System.currentTimeMillis()
)
