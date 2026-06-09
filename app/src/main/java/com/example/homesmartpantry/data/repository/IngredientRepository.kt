package com.example.homesmartpantry.data.repository

import com.example.homesmartpantry.data.local.dao.IngredientDao
import com.example.homesmartpantry.data.local.dao.InventoryDao
import com.example.homesmartpantry.data.local.dao.InventoryEventDao
import com.example.homesmartpantry.data.local.dao.RecipeDao
import com.example.homesmartpantry.data.local.dao.ShoppingDao
import com.example.homesmartpantry.data.local.dao.TodayCookDao
import com.example.homesmartpantry.data.local.dao.TodayCookWithRecipe
import com.example.homesmartpantry.data.local.entity.InventoryEventEntity
import com.example.homesmartpantry.data.local.entity.RecipeEntity
import com.example.homesmartpantry.data.local.entity.ShoppingItemEntity
import com.example.homesmartpantry.data.local.dao.InventoryWithIngredient
import com.example.homesmartpantry.domain.model.Ingredient
import com.example.homesmartpantry.domain.model.InventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngredientRepository(
    private val ingredientDao: IngredientDao,
    private val inventoryDao: InventoryDao,
    private val eventDao: InventoryEventDao,
    private val recipeDao: RecipeDao,
    private val shoppingDao: ShoppingDao,
    private val todayCookDao: TodayCookDao
) {
    fun getAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredients().map { entities ->
            entities.map { Ingredient(it.id, it.name, it.unit, it.category) }
        }
    }

    suspend fun addIngredient(name: String, unit: String, category: String = "食材", imageUri: String? = null): Long {
        val entity = com.example.homesmartpantry.data.local.entity.IngredientEntity(
            name = name,
            unit = unit,
            category = category,
            imageUri = imageUri
        )
        return ingredientDao.insert(entity)
    }

    fun getAllInventory(): Flow<List<InventoryItem>> {
        return inventoryDao.getAllInventory().map { items ->
            items.map { it.toDomainModel() }
        }
    }

    fun getUnsyncedEvents(): Flow<List<InventoryEventEntity>> {
        return eventDao.getUnsyncedEvents()
    }

    suspend fun addInventory(
        ingredientId: Long,
        quantity: Double,
        expireDate: Long? = null,
        storageLocation: String = "冰箱冷藏",
        purchaseDate: Long? = null,
        price: Double? = null,
        familyId: String = "default",
        deviceId: String = ""
    ): Long {
        val inventoryEntity = com.example.homesmartpantry.data.local.entity.InventoryEntity(
            familyId = familyId,
            ingredientId = ingredientId,
            quantity = quantity,
            expireDate = expireDate,
            storageLocation = storageLocation,
            purchaseDate = purchaseDate,
            price = price
        )
        val inventoryId = inventoryDao.insert(inventoryEntity)

        eventDao.insert(
            InventoryEventEntity(
                familyId = familyId,
                type = "ADD",
                ingredientId = ingredientId,
                quantity = quantity,
                deviceId = deviceId
            )
        )
        return inventoryId
    }

    suspend fun updateQuantity(id: Long, newQuantity: Double, deviceId: String = "") {
        val existing = inventoryDao.getById(id) ?: return
        val delta = newQuantity - existing.quantity
        inventoryDao.update(existing.copy(quantity = newQuantity))

        eventDao.insert(
            InventoryEventEntity(
                type = "UPDATE",
                ingredientId = existing.ingredientId,
                quantity = delta,
                deviceId = deviceId
            )
        )
    }

    suspend fun deleteInventory(id: Long, deviceId: String = "") {
        val existing = inventoryDao.getById(id) ?: return
        inventoryDao.deleteById(id)

        eventDao.insert(
            InventoryEventEntity(
                type = "USE",
                ingredientId = existing.ingredientId,
                quantity = -existing.quantity,
                deviceId = deviceId
            )
        )
    }

    suspend fun getExpiredItems(): List<InventoryItem> {
        val now = System.currentTimeMillis()
        return inventoryDao.getExpiredItems(now).map { entity ->
            val ingredient = ingredientDao.getIngredientById(entity.ingredientId)
            InventoryItem(
                id = entity.id,
                ingredientId = entity.ingredientId,
                ingredientName = ingredient?.name ?: "未知",
                unit = ingredient?.unit ?: "",
                quantity = entity.quantity,
                expireDate = entity.expireDate,
                category = ingredient?.category ?: "食材",
                storageLocation = entity.storageLocation,
                purchaseDate = entity.purchaseDate,
                price = entity.price,
                imageUri = ingredient?.imageUri
            )
        }
    }

    suspend fun getExpiringSoonItems(): List<InventoryItem> {
        val now = System.currentTimeMillis()
        val threshold = now + 3 * 24 * 60 * 60 * 1000L
        return inventoryDao.getExpiringSoon(now, threshold).map { entity ->
            val ingredient = ingredientDao.getIngredientById(entity.ingredientId)
            InventoryItem(
                id = entity.id,
                ingredientId = entity.ingredientId,
                ingredientName = ingredient?.name ?: "未知",
                unit = ingredient?.unit ?: "",
                quantity = entity.quantity,
                expireDate = entity.expireDate,
                category = ingredient?.category ?: "食材",
                storageLocation = entity.storageLocation,
                purchaseDate = entity.purchaseDate,
                price = entity.price,
                imageUri = ingredient?.imageUri
            )
        }
    }

    // ── Recipe operations ──

    private fun RecipeEntity.toDomain() = com.example.homesmartpantry.domain.model.Recipe(
        id, name, description, imageUri, category, difficulty, cookTime, servings,
        isFavorite, rating, calories, protein, fat, notes
    )

    fun getAllRecipes(): Flow<List<com.example.homesmartpantry.domain.model.Recipe>> {
        return recipeDao.getAllRecipes().map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getRecipeById(id: Long): com.example.homesmartpantry.domain.model.Recipe? {
        return recipeDao.getRecipeById(id)?.toDomain()
    }

    suspend fun saveRecipe(
        id: Long?,
        name: String,
        description: String,
        imageUri: String?,
        category: String,
        difficulty: String,
        cookTime: String,
        servings: String,
        calories: String,
        protein: String,
        fat: String,
        notes: String,
        ingredients: List<com.example.homesmartpantry.domain.model.RecipeIngredient>,
        steps: List<com.example.homesmartpantry.domain.model.RecipeStep>,
        tags: List<String>
    ): Long {
        val recipeId = if (id != null && id > 0) {
            recipeDao.update(id, name, description, imageUri, category, difficulty, cookTime,
                servings, calories, protein, fat, notes)
            id
        } else {
            recipeDao.insert(com.example.homesmartpantry.data.local.entity.RecipeEntity(
                name = name, description = description, imageUri = imageUri,
                category = category, difficulty = difficulty, cookTime = cookTime,
                servings = servings, calories = calories, protein = protein, fat = fat, notes = notes
            ))
        }

        // Replace all ingredients
        recipeDao.deleteRecipeIngredients(recipeId)
        recipeDao.insertRecipeIngredients(ingredients.map {
            com.example.homesmartpantry.data.local.entity.RecipeIngredientEntity(
                recipeId = recipeId, ingredientName = it.ingredientName,
                quantity = it.quantity, unit = it.unit
            )
        })

        // Replace all steps
        recipeDao.deleteRecipeSteps(recipeId)
        recipeDao.insertRecipeSteps(steps.mapIndexed { index, step ->
            com.example.homesmartpantry.data.local.entity.RecipeStepEntity(
                recipeId = recipeId, stepNumber = index + 1,
                description = step.description, imageUri = step.imageUri
            )
        })

        // Replace all tags
        recipeDao.deleteRecipeTags(recipeId)
        recipeDao.insertRecipeTags(tags.map {
            com.example.homesmartpantry.data.local.entity.RecipeTagEntity(recipeId = recipeId, tag = it)
        })

        return recipeId
    }

    suspend fun deleteRecipe(id: Long) { recipeDao.deleteById(id) }

    suspend fun setFavorite(id: Long, favorite: Boolean) { recipeDao.setFavorite(id, favorite) }

    fun getRecipeIngredients(recipeId: Long): Flow<List<com.example.homesmartpantry.domain.model.RecipeIngredient>> {
        return recipeDao.getRecipeIngredients(recipeId).map { entities ->
            entities.map {
                com.example.homesmartpantry.domain.model.RecipeIngredient(
                    recipeId = it.recipeId, ingredientName = it.ingredientName,
                    quantity = it.quantity, unit = it.unit
                )
            }
        }
    }

    suspend fun getRecipeIngredientsOnce(recipeId: Long): List<com.example.homesmartpantry.domain.model.RecipeIngredient> {
        return recipeDao.getRecipeIngredientsOnce(recipeId).map {
            com.example.homesmartpantry.domain.model.RecipeIngredient(
                recipeId = it.recipeId, ingredientName = it.ingredientName,
                quantity = it.quantity, unit = it.unit
            )
        }
    }

    fun getRecipeSteps(recipeId: Long): Flow<List<com.example.homesmartpantry.domain.model.RecipeStep>> {
        return recipeDao.getRecipeSteps(recipeId).map { entities ->
            entities.map {
                com.example.homesmartpantry.domain.model.RecipeStep(
                    id = it.id, recipeId = it.recipeId, stepNumber = it.stepNumber,
                    description = it.description, imageUri = it.imageUri
                )
            }
        }
    }

    suspend fun getRecipeStepsOnce(recipeId: Long): List<com.example.homesmartpantry.domain.model.RecipeStep> {
        return recipeDao.getRecipeStepsOnce(recipeId).map {
            com.example.homesmartpantry.domain.model.RecipeStep(
                id = it.id, recipeId = it.recipeId, stepNumber = it.stepNumber,
                description = it.description, imageUri = it.imageUri
            )
        }
    }

    fun getRecipeTags(recipeId: Long): Flow<List<String>> = recipeDao.getRecipeTags(recipeId)

    suspend fun getRecipeTagsOnce(recipeId: Long): List<String> = recipeDao.getRecipeTagsOnce(recipeId)

    // ── Shopping list ──

    fun getShoppingItems(): Flow<List<ShoppingItemEntity>> = shoppingDao.getAllItems()

    fun getUnpurchasedCount(): Flow<Int> = shoppingDao.getUnpurchasedCount()

    suspend fun addToShoppingList(items: List<com.example.homesmartpantry.domain.model.ShoppingItem>) {
        shoppingDao.insertAll(items.map {
            ShoppingItemEntity(
                ingredientName = it.ingredientName, quantity = it.quantity,
                unit = it.unit, sourceRecipeId = it.sourceRecipeId
            )
        })
    }

    suspend fun markPurchased(id: Long) = shoppingDao.markPurchased(id)
    suspend fun deleteShoppingItem(id: Long) = shoppingDao.deleteById(id)
    suspend fun clearPurchased() = shoppingDao.clearPurchased()

    suspend fun getInventoryById(id: Long): com.example.homesmartpantry.data.local.entity.InventoryEntity? = inventoryDao.getById(id)
    suspend fun updateInventoryEntity(entity: com.example.homesmartpantry.data.local.entity.InventoryEntity) = inventoryDao.update(entity)

    // ── Today cook list ──

    fun getTodayCookList(): Flow<List<TodayCookWithRecipe>> = todayCookDao.getTodayCookList()

    suspend fun addToTodayCook(recipeId: Long) {
        todayCookDao.insert(com.example.homesmartpantry.data.local.entity.TodayCookEntity(recipeId))
    }

    suspend fun removeFromTodayCook(recipeId: Long) {
        todayCookDao.deleteByRecipeId(recipeId)
    }

    suspend fun clearOldTodayCookEntries(todayStart: Long) {
        todayCookDao.clearOldEntries(todayStart)
    }

    suspend fun isInTodayCook(recipeId: Long): Boolean {
        return todayCookDao.getByRecipeId(recipeId) != null
    }

    fun getTodayCookCount(): Flow<Int> = todayCookDao.getCount()
}

private fun InventoryWithIngredient.toDomainModel() = InventoryItem(
    id = id,
    ingredientId = ingredientId,
    ingredientName = ingredientName,
    unit = unit,
    quantity = quantity,
    expireDate = expireDate,
    category = category,
    storageLocation = storageLocation,
    purchaseDate = purchaseDate,
    price = price,
    imageUri = imageUri
)
