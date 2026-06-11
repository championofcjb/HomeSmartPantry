package com.example.homesmartpantry.presentation.screen.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onAddedToTodayCook: () -> Unit = {}
) {
    val detailState by viewModel.detailState.collectAsState()
    val isInTodayCook by viewModel.isInTodayCook.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(recipeId) {
        viewModel.loadDetail(recipeId)
        viewModel.loadTodayCookDetail(recipeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detailState.recipe?.name ?: "菜谱详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(recipeId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val wasIn = isInTodayCook
                        viewModel.toggleTodayCook(recipeId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (wasIn) "已从今日做菜移除" else "已加入今日做菜"
                            )
                        }
                        onAddedToTodayCook()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = if (isInTodayCook) {
                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Icon(
                        if (isInTodayCook) Icons.Default.CheckCircle else Icons.Default.Restaurant,
                        null, Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isInTodayCook) "已在今日" else "今日做菜")
                }
                Button(
                    onClick = {
                        viewModel.cookToday { result ->
                            scope.launch {
                                when (result) {
                                    is RecipeViewModel.CookTodayResult.CanCook -> {
                                        snackbarHostState.showSnackbar("✅ 食材充足，可以开始制作！")
                                    }
                                    is RecipeViewModel.CookTodayResult.AddToCart -> {
                                        snackbarHostState.showSnackbar("已将全部食材加入采购清单（${result.count}种）")
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text("开始做菜") }
            }
        }
    ) { padding ->
        if (detailState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val recipe = detailState.recipe ?: return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Image banner
            item {
                val gradient = when (recipe.category) {
                    "川菜" -> Brush.horizontalGradient(listOf(Color(0xFFDC3545), Color(0xFFFF6B6B)))
                    "粤菜" -> Brush.horizontalGradient(listOf(Color(0xFF28A745), Color(0xFF69DB7C)))
                    "烘焙" -> Brush.horizontalGradient(listOf(Color(0xFFE67E22), Color(0xFFF0C27A)))
                    "汤羹" -> Brush.horizontalGradient(listOf(Color(0xFF17A2B8), Color(0xFF67D5E2)))
                    "凉菜" -> Brush.horizontalGradient(listOf(Color(0xFF6610F2), Color(0xFF9D6FFA)))
                    "主食" -> Brush.horizontalGradient(listOf(Color(0xFF6F42C1), Color(0xFFA379E0)))
                    else -> Brush.horizontalGradient(listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    ))
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (recipe.imageUri != null) {
                        AsyncImage(
                            model = recipe.imageUri,
                            contentDescription = "菜谱图片",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(gradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🍳", style = MaterialTheme.typography.displayLarge)
                                Spacer(Modifier.height(8.dp))
                                Text(recipe.category.ifBlank { "家常菜" },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }
                }
            }

            // Name + info
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(recipe.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(recipe.category)
                        InfoChip(recipe.cookTime)
                        InfoChip(recipe.difficulty)
                        InfoChip(recipe.servings)
                    }
                    // Description
                    if (recipe.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(5) { i ->
                                Icon(
                                    imageVector = if (i < recipe.rating.toInt()) Icons.Default.Star
                                                  else Icons.Default.StarBorder,
                                    contentDescription = "评分 ${i + 1}",
                                    tint = Color(0xFFFBBC05),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { viewModel.setRating((i + 1).toFloat()) }
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (recipe.isFavorite) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder,
                                contentDescription = if (recipe.isFavorite) "取消收藏" else "收藏",
                                tint = if (recipe.isFavorite) Color(0xFFEA4335)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Stock status
            item { StockStatusBanner(detailState.ingredients, viewModel, snackbarHostState, scope) }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // Ingredients
            item { SectionTitle("所需食材") }

            itemsIndexed(detailState.ingredients) { _, ing -> IngredientRow(ing) }

            // Add to shopping list
            if (detailState.ingredients.any { it.status != IngredientStockStatus.SUFFICIENT }) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            viewModel.addMissingToShoppingList()
                            scope.launch { snackbarHostState.showSnackbar("已加入采购清单") }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853))
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("加入采购清单")
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // Steps
            item { SectionTitle("制作步骤") }

            if (detailState.steps.isEmpty()) {
                item {
                    Text("暂无步骤描述", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }

            itemsIndexed(detailState.steps) { index, step ->
                StepCard(index + 1, step.description)
            }

            // Nutrition
            if (recipe.calories.isNotBlank() || recipe.protein.isNotBlank() || recipe.fat.isNotBlank()) {
                item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
                item { SectionTitle("营养信息") }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (recipe.calories.isNotBlank()) {
                            Column(Modifier.weight(1f)) {
                                NutritionCard("热量", recipe.calories, 0xFFEA4335)
                            }
                        }
                        if (recipe.protein.isNotBlank()) {
                            Column(Modifier.weight(1f)) {
                                NutritionCard("蛋白质", recipe.protein, 0xFF4285F4)
                            }
                        }
                        if (recipe.fat.isNotBlank()) {
                            Column(Modifier.weight(1f)) {
                                NutritionCard("脂肪", recipe.fat, 0xFFFBBC05)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Notes
            if (recipe.notes.isNotBlank()) {
                item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
                item { SectionTitle("备注") }
                item {
                    Text(recipe.notes, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }

            // Tags
            if (detailState.tags.isNotEmpty()) {
                item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
                item { SectionTitle("标签") }
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        detailState.tags.forEach { tag -> InfoChip(tag) }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Helpers ──

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
}

@Composable
private fun StockStatusBanner(
    ingredients: List<IngredientWithStock>,
    viewModel: RecipeViewModel,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val sufficient = ingredients.count { it.status == IngredientStockStatus.SUFFICIENT }
    val missing = ingredients.size - sufficient
    val total = ingredients.size

    val icon: String
    val text: String
    val bgColor: Color
    val textColor: Color
    when {
        missing == 0 -> { icon = "✅"; text = "当前库存可直接制作"
            bgColor = Color(0xFF34A853).copy(alpha = 0.12f); textColor = Color(0xFF34A853) }
        sufficient > 0 -> { icon = "⚠"; text = "缺少${missing}种食材"
            bgColor = Color(0xFFFBBC05).copy(alpha = 0.12f); textColor = Color(0xFFB8860B) }
        else -> { icon = "❌"; text = "缺少主要食材"
            bgColor = Color(0xFFEA4335).copy(alpha = 0.12f); textColor = Color(0xFFEA4335) }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium, color = textColor)
                Text("食材匹配: $sufficient/$total",
                    style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.7f))
            }
            if (missing > 0) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    viewModel.addMissingToShoppingList()
                    scope.launch { snackbarHostState.showSnackbar("已加入采购清单") }
                }) {
                    Icon(Icons.Default.ShoppingCart, "加入采购清单",
                        tint = textColor, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ing: IngredientWithStock) {
    val (statusColor, statusBadge) = when (ing.status) {
        IngredientStockStatus.SUFFICIENT -> Color(0xFF34A853) to "✅ 充足"
        IngredientStockStatus.INSUFFICIENT -> Color(0xFFB8860B) to "⚠ 不足"
        IngredientStockStatus.MISSING -> Color(0xFFEA4335) to "❌ 缺"
    }
    val stockText = if (ing.stockQty.isNotBlank()) "库存 ${ing.stockQty} ${ing.stockUnit}" else "暂无库存"

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ing.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("需要: ${ing.requiredQty} ${ing.unit}  ·  $stockText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(statusBadge, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, color = statusColor)
    }
}

@Composable
private fun StepCard(number: Int, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(description, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun InfoChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun NutritionCard(label: String, value: String, colorVal: Long) {
    val color = Color(colorVal)
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
