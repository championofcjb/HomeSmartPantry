package com.example.homesmartpantry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homesmartpantry.data.local.entity.TodayCookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayCookDao {
    /** 今日待做列表（含菜谱名称） */
    @Query("""
        SELECT r.id, r.name, r.description, r.imageUri, r.category, r.difficulty,
               r.cookTime, r.servings, r.isFavorite, r.rating, r.calories,
               r.protein, r.fat, r.notes, r.createDate, tc.addedDate
        FROM today_cook_list tc
        INNER JOIN recipes r ON r.id = tc.recipeId
        ORDER BY tc.addedDate DESC
    """)
    fun getTodayCookList(): Flow<List<TodayCookWithRecipe>>

    /** 插入或更新（同一菜谱只保留一条，今天的记录覆盖） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TodayCookEntity)

    /** 从今日做菜列表移除 */
    @Query("DELETE FROM today_cook_list WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)

    /** 删除昨日及更早的记录 */
    @Query("DELETE FROM today_cook_list WHERE addedDate < :todayStart")
    suspend fun clearOldEntries(todayStart: Long)

    /** 查询某菜谱是否在今日列表 */
    @Query("SELECT recipeId FROM today_cook_list WHERE recipeId = :recipeId")
    suspend fun getByRecipeId(recipeId: Long): Long?

    /** 今日列表数量 */
    @Query("SELECT COUNT(*) FROM today_cook_list")
    fun getCount(): Flow<Int>
}

data class TodayCookWithRecipe(
    val id: Long,
    val name: String,
    val description: String,
    val imageUri: String?,
    val category: String,
    val difficulty: String,
    val cookTime: String,
    val servings: String,
    val isFavorite: Boolean,
    val rating: Float,
    val calories: String,
    val protein: String,
    val fat: String,
    val notes: String,
    val createDate: Long,
    val addedDate: Long
)
