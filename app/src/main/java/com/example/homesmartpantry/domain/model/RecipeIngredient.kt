package com.example.homesmartpantry.domain.model

data class RecipeIngredient(
    val recipeId: Long,
    val ingredientName: String,
    val quantity: String,
    val unit: String
) {
    val displayText: String get() = "$ingredientName $quantity$unit"
}
