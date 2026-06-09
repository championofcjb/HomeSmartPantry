package com.example.homesmartpantry.domain.model

data class RecipeStep(
    val id: Long = 0,
    val recipeId: Long,
    val stepNumber: Int,
    val description: String,
    val imageUri: String? = null
)
