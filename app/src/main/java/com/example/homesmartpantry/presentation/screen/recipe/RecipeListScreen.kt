package com.example.homesmartpantry.presentation.screen.recipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.homesmartpantry.domain.model.Recipe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    onAddClick: () -> Unit,
    onRecipeClick: (Long) -> Unit,
    onFavoritesClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    var selectedCategory by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selection mode top bar
        if (isSelectionMode) {
            val currentFiltered = getFilteredRecipes(uiState.recipes, selectedCategory)
            val allIds = currentFiltered.map { it.recipe.id }
            val allSelected = selectedIds.size == allIds.size
            TopAppBar(
                title = { Text("已选 ${selectedIds.size} 项") },
                navigationIcon = {
                    IconButton(onClick = { selectedIds = emptySet() }) {
                        Icon(Icons.Default.Close, contentDescription = "取消选择")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        selectedIds = if (allSelected) emptySet() else allIds.toSet()
                    }) {
                        Text(if (allSelected) "取消全选" else "全选",
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = {
                        selectedIds.forEach { viewModel.deleteRecipe(it) }
                        selectedIds = emptySet()
                    }) {
                        Icon(Icons.Default.Delete, "删除选中",
                            tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        } else {
            // Filter chips
            val tabs = listOf("全部", "可做", "部分可做", "收藏")
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    FilterChip(selected = uiState.selectedTab == index, onClick = { viewModel.setTab(index) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                }
                IconButton(onClick = onFavoritesClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = "收藏菜谱",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Category filter chips
            val allCategories = uiState.recipes.map { it.recipe.category }.distinct().filter { it.isNotBlank() }.sorted()
            if (allCategories.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = selectedCategory.isEmpty(), onClick = { selectedCategory = "" },
                        label = { Text("全部", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer))
                    allCategories.take(8).forEach { cat ->
                        FilterChip(selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) "" else cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }

            // Sort chips
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val sortOptions = listOf(
                    com.example.homesmartpantry.presentation.screen.recipe.RecipeSortOption.NAME to "名称",
                    com.example.homesmartpantry.presentation.screen.recipe.RecipeSortOption.RATING to "评分",
                    com.example.homesmartpantry.presentation.screen.recipe.RecipeSortOption.NEWEST to "最新")
                sortOptions.forEach { (option, label) ->
                    FilterChip(selected = uiState.sortOption == option, onClick = { viewModel.setSortOption(option) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer))
                }
            }

            // Search bar
            OutlinedTextField(value = searchText, onValueChange = { searchText = it; viewModel.setSearchQuery(it) },
                placeholder = { Text("搜索菜谱...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                trailingIcon = { if (searchText.isNotEmpty()) {
                    IconButton(onClick = { searchText = ""; viewModel.setSearchQuery("") }) { Icon(Icons.Default.Close, contentDescription = "清除") } } },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true, shape = MaterialTheme.shapes.medium)
        }

        // Content area
        val currentFiltered = getFilteredRecipes(uiState.recipes, selectedCategory)

        if (currentFiltered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = if (searchText.isNotBlank()) "没有找到匹配的菜谱"
                           else "还没有菜谱\n点击右下角 + 添加吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentFiltered, key = { it.recipe.id }) { item ->
                    val isSelected = selectedIds.contains(item.recipe.id)
                    RecipeCard(
                        recipeWithStatus = item,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                selectedIds = if (isSelected) selectedIds - item.recipe.id
                                              else selectedIds + item.recipe.id
                            } else {
                                onRecipeClick(item.recipe.id)
                            }
                        },
                        onLongClick = { if (!isSelectionMode) selectedIds = selectedIds + item.recipe.id },
                        onDelete = { viewModel.deleteRecipe(item.recipe.id) }
                    )
                }
            }
        }
    }
}

private fun getFilteredRecipes(recipes: List<RecipeWithStatus>, category: String): List<RecipeWithStatus> {
    return if (category.isBlank()) recipes
    else recipes.filter { it.recipe.category == category }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecipeCard(
    recipeWithStatus: RecipeWithStatus,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDelete: () -> Unit
) {
    val (statusIcon, statusColor) = when (recipeWithStatus.availability) {
        Availability.FULL -> Pair(Icons.Default.CheckCircle, Color(0xFF34A853))
        Availability.PARTIAL -> Pair(Icons.Default.Info, Color(0xFFFBBC05))
        Availability.NONE -> Pair(Icons.Default.ErrorOutline, Color(0xFFEA4335))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isSelectionMode) 8.dp else 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = recipeWithStatus.recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (recipeWithStatus.recipe.category.isNotBlank()) {
                        Text(
                            text = recipeWithStatus.recipe.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "食材: ${recipeWithStatus.matchCount}/${recipeWithStatus.totalCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (recipeWithStatus.availability == Availability.PARTIAL &&
                    recipeWithStatus.missingIngredients.isNotEmpty()
                ) {
                    Text(
                        text = "缺: ${recipeWithStatus.missingIngredients.take(3).joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                val daysSince = (System.currentTimeMillis() - recipeWithStatus.recipe.createDate) /
                    (24 * 60 * 60 * 1000)
                Text(
                    text = when {
                        daysSince < 1 -> "今天创建"
                        daysSince == 1L -> "昨天创建"
                        daysSince < 7 -> "${daysSince}天前创建"
                        daysSince < 30 -> "${daysSince / 7}周前创建"
                        else -> "${daysSince / 30}个月前创建"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (recipeWithStatus.recipe.isFavorite) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "收藏",
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.padding(end = 4.dp).size(18.dp)
                )
            }
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.padding(end = 8.dp)
            )

            if (!isSelectionMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
