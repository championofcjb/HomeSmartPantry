package com.example.homesmartpantry.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "recipe_tags",
    primaryKeys = ["recipeId", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeTagEntity(
    val recipeId: Long,
    val tag: String
)
