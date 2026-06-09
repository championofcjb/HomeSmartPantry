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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "HomeSmartPantry",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Tab state (hoisted to composable level)
        val tabTitles = listOf("全部", "食材", "调味料", "主食粮油")
        var selectedTab by remember { mutableStateOf(0) }

        // Search bar
        if (!isSelectionMode) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("搜索食材...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
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

        // Filtered inventory (re-computed when tab changes)
        val filteredInventory = if (isSelectionMode) {
            uiState.inventory
        } else when (selectedTab) {
            0 -> uiState.inventory
            1 -> uiState.inventory.filter { it.category == "食材" || it.category == "其他" }
            2 -> uiState.inventory.filter { it.category == "调味料" }
            3 -> uiState.inventory.filter { it.category == "主食粮油" }
            else -> uiState.inventory
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
                            }
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
    onUpdateQuantity: (Double) -> Unit
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
            }
        }
    }

    // Quantity edit dialog
    if (showQuantityDialog && !isSelectionMode) {
        QuantityEditDialog(
            currentQuantity = item.quantity,
            unit = item.unit,
            onConfirm = { newQty ->
                onUpdateQuantity(newQty)
                showQuantityDialog = false
            },
            onDismiss = { showQuantityDialog = false }
        )
    }
}

@Composable
private fun QuantityEditDialog(
    currentQuantity: Double,
    unit: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(formatQuantity(currentQuantity)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改数量") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { newVal ->
                    if (newVal.isEmpty() || newVal.matches(Regex("^\\d*\\.?\\d*$"))) {
                        input = newVal
                    }
                },
                label = { Text("数量") },
                suffix = { Text(unit) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = input.toDoubleOrNull()
                    if (qty != null && qty >= 0) {
                        onConfirm(qty)
                    }
                },
                enabled = input.toDoubleOrNull() != null
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
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
