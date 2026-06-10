package com.example.homesmartpantry.domain.model

data class Recipe(
    val id: Long,
    val name: String,
    val description: String = "",
    val imageUri: String? = null,
    val category: String = "家常菜",
    val difficulty: String = "普通",
    val cookTime: String = "30分钟",
    val servings: String = "2人份",
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val notes: String = "",
    val createDate: Long = System.currentTimeMillis()
)
