package com.example.homesmartpantry.domain.model

data class InventoryItem(
    val id: Long,
    val ingredientId: Long,
    val ingredientName: String,
    val unit: String,
    val quantity: Double,
    val expireDate: Long? = null,
    val category: String = "食材",
    val storageLocation: String = "冰箱冷藏",
    val purchaseDate: Long? = null,
    val price: Double? = null,
    val imageUri: String? = null
) {
    val isExpiringSoon: Boolean
        get() {
            expireDate ?: return false
            val threeDaysFromNow = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
            return expireDate <= threeDaysFromNow
        }

    val isExpired: Boolean
        get() {
            expireDate ?: return false
            return expireDate <= System.currentTimeMillis()
        }
}
