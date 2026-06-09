package com.example.homesmartpantry.domain.model

data class ShoppingItem(
    val id: Long = 0,
    val ingredientName: String,
    val quantity: String = "",
    val unit: String = "",
    val isPurchased: Boolean = false,
    val sourceRecipeId: Long? = null
)
