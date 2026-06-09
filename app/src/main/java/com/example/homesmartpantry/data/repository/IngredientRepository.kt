package com.example.homesmartpantry.data.repository

import com.example.homesmartpantry.data.local.dao.IngredientDao
import com.example.homesmartpantry.data.local.dao.InventoryDao
import com.example.homesmartpantry.data.local.dao.InventoryEventDao
import com.example.homesmartpantry.data.local.dao.RecipeDao
import com.example.homesmartpantry.data.local.entity.InventoryEventEntity
import com.example.homesmartpantry.data.local.dao.InventoryWithIngredient
import com.example.homesmartpantry.domain.model.Ingredient
import com.example.homesmartpantry.domain.model.InventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngredientRepository(
    private val ingredientDao: IngredientDao,
    private val inventoryDao: InventoryDao,
    private val eventDao: InventoryEventDao,
    private val recipeDao: RecipeDao
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
