package com.example.homesmartpantry.domain.model

data class Ingredient(
    val id: Long,
    val name: String,
    val unit: String,
    val category: String = "食材",
    val imageUri: String? = null
)
