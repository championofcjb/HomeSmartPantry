package com.example.homesmartpantry.presentation.navigation

object NavRoutes {
    const val HOME = "home"
    const val ADD_INGREDIENT = "add_ingredient"
    const val RECIPES = "recipes"
    const val ADD_RECIPE = "add_recipe"
    const val EDIT_RECIPE = "edit_recipe/{recipeId}"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val SETTINGS = "settings"
    const val SHOPPING_LIST = "shopping_list"
    const val TODAY_COOK = "today_cook"

    fun recipeDetail(id: Long) = "recipe_detail/$id"
    fun editRecipe(id: Long) = "edit_recipe/$id"
}
