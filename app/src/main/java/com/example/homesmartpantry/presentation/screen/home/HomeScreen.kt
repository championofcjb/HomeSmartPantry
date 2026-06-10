package com.example.homesmartpantry.presentation.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.homesmartpantry.domain.model.InventoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onRecipesClick: () -> Unit = {},
    onShoppingListClick: () -> Unit = {},
    onEditItemClick: (Long) -> Unit = {},
    onSelectionModeChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    // Notify MainActivity about selection mode
    androidx.compose.runtime.LaunchedEffect(isSelectionMode) {
        onSelectionModeChanged(isSelectionMode)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar — switches between normal and selection mode
        if (isSelectionMode) {
            val allIds = uiState.inventory.map { it.id }
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
                        Text(
                            if (allSelected) "取消全选" else "全选",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    TextButton(onClick = {
                        selectedIds = uiState.inventory.filter { it.isExpired }.map { it.id }.toSet()
                    }) {
                        Text("过期", color = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = {
                        viewModel.deleteItems(selectedIds.toList())
                        selectedIds = emptySet()
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除选中",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Text(
                    text = "HomeSmartPantry",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                IconButton(
                    onClick = onShoppingListClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    BadgedBox(badge = {
                        if (uiState.shoppingCount > 0) {
                            Badge { Text("${uiState.shoppingCount}", style = MaterialTheme.typography.labelSmall) }
                        }
                    }) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "采购清单",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Expiry warning banner
        if (!isSelectionMode && (uiState.expiredCount > 0 || uiState.expiringSoonCount > 0)) {
            val (bgColor, textColor) = if (uiState.expiredCount > 0) {
                MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
            }
            Card(
                onClick = {
                    selectedIds = uiState.inventory.filter { it.isExpired }.map { it.id }.toSet()
                    if (selectedIds.isEmpty()) {
                        selectedIds = uiState.inventory.filter { it.isExpiringSoon }.map { it.id }.toSet()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null,
                        tint = if (uiState.expiredCount > 0) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        val message = buildString {
                            if (uiState.expiredCount > 0) {
                                append("${uiState.expiredCount} 样食材已过期")
                            }
                            if (uiState.expiringSoonCount > 0) {
                                if (isNotEmpty()) append("，")
                                append("${uiState.expiringSoonCount} 样即将过期")
                            }
                        }
                        Text(text = message, style = MaterialTheme.typography.titleSmall,
                            color = textColor)
                        Text(text = "请及时处理", style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Today's recommendation card
        if (!isSelectionMode && (uiState.cookable.fullCount > 0 || uiState.cookable.partialCount > 0)) {
            Card(
                onClick = onRecipesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🍳", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        val sb = StringBuilder()
                        if (uiState.cookable.fullCount > 0) sb.append("今天可以做 ${uiState.cookable.fullCount} 道菜")
                        if (uiState.cookable.partialCount > 0) {
                            if (sb.isNotEmpty()) sb.append(" · ")
                            sb.append("部分可做 ${uiState.cookable.partialCount} 道")
                        }
                        Text(text = sb.toString(), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        if (uiState.cookable.topRecipes.isNotEmpty()) {
                            Text(text = uiState.cookable.topRecipes.joinToString("、"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                    }
                    Icon(Icons.Default.Add, contentDescription = "查看菜谱",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Inventory stats row
        if (!isSelectionMode && uiState.inventory.isNotEmpty()) {
            val categoryCount = uiState.inventory.map { it.category }.distinct().size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(value = "${uiState.inventory.size}", label = "总物品")
                StatItem(value = "$categoryCount", label = "分类")
                StatItem(value = "${uiState.inventory.count { !it.isExpired && !it.isExpiringSoon }}", label = "正常")
                StatItem(value = "${uiState.expiredCount + uiState.expiringSoonCount}", label = "需处理")
            }
        }

        // Tab state (hoisted to composable level)
        val tabTitles = listOf("全部", "食材", "调味料", "主食粮油")
        var selectedTab by remember { mutableStateOf(0) }
        var searchQuery by remember { mutableStateOf("") }

        // Search bar
        if (!isSelectionMode) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索食材...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "清除")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Category sub-tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    androidx.compose.material3.Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = if (selectedTab == index)
                                    MaterialTheme.typography.titleMedium
                                else
                                    MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            }
        }

        // Sort chips
        if (!isSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val sortOptions = listOf(
                    com.example.homesmartpantry.presentation.screen.home.SortOption.NAME to "名称",
                    com.example.homesmartpantry.presentation.screen.home.SortOption.EXPIRY to "过期",
                    com.example.homesmartpantry.presentation.screen.home.SortOption.CATEGORY to "分类",
                    com.example.homesmartpantry.presentation.screen.home.SortOption.QUANTITY to "数量"
                )
                sortOptions.forEach { (option, label) ->
                    FilterChip(
                        selected = uiState.sortOption == option,
                        onClick = { viewModel.setSortOption(option) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }

        // Filtered inventory (re-computed when tab or search changes)
        val filteredByTab = if (isSelectionMode) {
            uiState.inventory
        } else when (selectedTab) {
            0 -> uiState.inventory
            1 -> uiState.inventory.filter { it.category == "食材" || it.category == "其他" }
            2 -> uiState.inventory.filter { it.category == "调味料" }
            3 -> uiState.inventory.filter { it.category == "主食粮油" }
            else -> uiState.inventory
        }
        val filteredInventory = if (searchQuery.isBlank()) filteredByTab
        else filteredByTab.filter {
            val q = searchQuery.lowercase()
            it.ingredientName.contains(q, ignoreCase = true) ||
            it.storageLocation.contains(q, ignoreCase = true) ||
            it.category.contains(q, ignoreCase = true)
        }

        // Content — always visible
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            filteredInventory.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSelectionMode) "没有可选的物品" else "该分类下还没有物品\n点击右下角 + 添加吧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredInventory, key = { it.id }) { item ->
                        val isSelected = selectedIds.contains(item.id)
                        InventoryCard(
                            item = item,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelectionMode) {
                                    selectedIds = if (isSelected) {
                                        selectedIds - item.id
                                    } else {
                                        selectedIds + item.id
                                    }
                                }
                            },
                            onLongClick = {
                                selectedIds = selectedIds + item.id
                            },
                            onUpdateQuantity = { newQty ->
                                viewModel.updateQuantity(item.id, newQty)
                            },
                            onEditClick = { onEditItemClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryCard(
    item: InventoryItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUpdateQuantity: (Double) -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    var showQuantityDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onClick()
                    } else {
                        showQuantityDialog = true
                    }
                },
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
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox in selection mode
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Category emoji icon
            if (!isSelectionMode) {
                Text(
                    text = categoryIcon(item.category),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.ingredientName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Quantity with +/- buttons (only in normal mode)
                if (isSelectionMode) {
                    Text(
                        text = "${formatQuantity(item.quantity)} ${item.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val half = item.quantity / 2
                                onUpdateQuantity(if (half > 0) half else 0.0)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "减半",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "${formatQuantity(item.quantity)} ${item.unit}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { onUpdateQuantity(item.quantity + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "加一",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Storage location + price
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.storageLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.price != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "¥${formatQuantity(item.price!!)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expire info
                if (item.expireDate != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val dateStr = SimpleDateFormat("MM/dd", Locale.getDefault())
                        .format(Date(item.expireDate))
                    val statusColor = when {
                        item.isExpired -> MaterialTheme.colorScheme.error
                        item.isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = "过期: $dateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }

                // Purchase date
                if (item.purchaseDate != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                        .format(Date(item.purchaseDate))
                    Text(
                        text = "购买: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Edit item dialog
    if (showQuantityDialog && !isSelectionMode) {
        ItemEditDialog(
            item = item,
            onConfirm = { newQty, newLoc, newPrice ->
                onUpdateQuantity(newQty)
                showQuantityDialog = false
            },
            onDismiss = { showQuantityDialog = false },
            onEditDetails = onEditClick
        )
    }
}

@Composable
private fun ItemEditDialog(
    item: InventoryItem,
    onConfirm: (Double, String, Double?) -> Unit,
    onDismiss: () -> Unit,
    onEditDetails: (() -> Unit)? = null
) {
    var qtyInput by remember { mutableStateOf(formatQuantity(item.quantity)) }
    var loc by remember { mutableStateOf(item.storageLocation) }
    var priceInput by remember { mutableStateOf(item.price?.let { formatQuantity(it) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑 ${item.ingredientName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) qtyInput = it },
                    label = { Text("数量") },
                    suffix = { Text(item.unit) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = loc,
                    onValueChange = { loc = it },
                    label = { Text("存放位置") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) priceInput = it },
                    label = { Text("价格") },
                    prefix = { Text("¥") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = qtyInput.toDoubleOrNull()
                    if (qty != null && qty >= 0) {
                        onConfirm(qty, loc, priceInput.toDoubleOrNull())
                    }
                },
                enabled = qtyInput.toDoubleOrNull() != null
            ) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onEditDetails != null) {
                    TextButton(onClick = {
                        onDismiss()
                        onEditDetails()
                    }) {
                        Text("详细编辑", color = MaterialTheme.colorScheme.primary)
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

private fun formatQuantity(qty: Double): String {
    return if (qty == qty.toLong().toDouble()) {
        qty.toLong().toString()
    } else {
        String.format("%.1f", qty)
    }
}

private fun categoryIcon(category: String): String = when {
    category.contains("调味") || category.contains("料") -> "🧂"
    category.contains("主食") || category.contains("米") || category.contains("面") -> "🍚"
    category.contains("蔬菜") || category.contains("菜") -> "🥬"
    category.contains("水果") || category.contains("果") -> "🍎"
    category.contains("肉") || category.contains("禽") -> "🥩"
    category.contains("海鲜") || category.contains("鱼") || category.contains("虾") -> "🦐"
    category.contains("蛋") || category.contains("奶") -> "🥛"
    category.contains("冷冻") || category.contains("冰") -> "🧊"
    category.contains("零食") || category.contains("小吃") -> "🍪"
    category.contains("饮品") || category.contains("饮料") || category.contains("酒") -> "🥤"
    category.contains("干货") || category.contains("干") -> "🥜"
    else -> "📦"
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
