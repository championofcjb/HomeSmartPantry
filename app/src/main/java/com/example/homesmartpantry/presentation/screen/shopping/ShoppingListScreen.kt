package com.example.homesmartpantry.presentation.screen.shopping

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.homesmartpantry.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListScreen(
    shoppingItems: Flow<List<ShoppingItemEntity>>,
    knownIngredients: List<com.example.homesmartpantry.domain.model.Ingredient> = emptyList(),
    onMarkPurchased: (Long) -> Unit,
    onMarkUnpurchased: (Long) -> Unit = {},
    onDelete: (Long) -> Unit,
    onClearPurchased: () -> Unit,
    onBack: () -> Unit,
    onAddItem: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateItem: (Long, String, String, String) -> Unit = { _, _, _, _ -> },
    onAddToInventory: (String, Double, String, String, Long?) -> Unit = { _, _, _, _, _ -> }
) {
    val items by shoppingItems.collectAsState(initial = emptyList())
    val unpurchased = items.filter { !it.isPurchased }
    val purchased = items.filter { it.isPurchased }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItemEntity?>(null) }
    var purchasingItem by remember { mutableStateOf<ShoppingItemEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("采购清单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (purchased.isNotEmpty()) {
                        TextButton(onClick = onClearPurchased) {
                            Text("清除已购", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加物品")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("采购清单为空", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("点击右下角 + 手动添加\n或从菜谱详情页添加", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Unpurchased
                if (unpurchased.isNotEmpty()) {
                    item {
                        Text("待采购 (${unpurchased.size})", style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                    }
                }

                items(unpurchased, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = {
                            if (item.isPurchased) {
                                onMarkUnpurchased(item.id)
                            } else {
                                purchasingItem = item
                            }
                        },
                        onDelete = { onDelete(item.id) },
                        onEdit = { editingItem = item }
                    )
                }

                // Purchased
                if (purchased.isNotEmpty()) {
                    item {
                        Text("已采购 (${purchased.size})", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    }
                }

                items(purchased, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = {
                            if (item.isPurchased) onMarkUnpurchased(item.id)
                            else onMarkPurchased(item.id)
                        },
                        onDelete = { onDelete(item.id) },
                        onEdit = { editingItem = item }
                    )
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        AddShoppingItemDialog(
            knownIngredients = knownIngredients,
            onConfirm = { name, qty, unit ->
                onAddItem(name, qty, unit)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit dialog
    editingItem?.let { item ->
        EditShoppingItemDialog(
            item = item,
            onConfirm = { name, qty, unit ->
                onUpdateItem(item.id, name, qty, unit)
                editingItem = null
            },
            onDismiss = { editingItem = null }
        )
    }

    // Purchase → add to inventory dialog
    purchasingItem?.let { item ->
        AddToInventoryDialog(
            itemName = item.ingredientName,
            defaultQuantity = item.quantity,
            defaultUnit = item.unit,
            onConfirm = { qty, loc, unit, expireDate ->
                onAddToInventory(item.ingredientName, qty, loc, unit, expireDate)
                onMarkPurchased(item.id)
                purchasingItem = null
            },
            onDismiss = {
                purchasingItem = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShoppingItemRow(
    item: ShoppingItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onEdit
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
            Icon(
                if (item.isPurchased) Icons.Default.CheckCircle
                else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (item.isPurchased) "取消已购" else "标记已购",
                tint = if (item.isPurchased) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.ingredientName,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            if (item.quantity.isNotBlank()) {
                Text("${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddShoppingItemDialog(
    knownIngredients: List<com.example.homesmartpantry.domain.model.Ingredient> = emptyList(),
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    val suggestions = if (name.isNotBlank()) {
        knownIngredients.filter { it.name.contains(name, ignoreCase = true) }.take(5)
    } else emptyList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加采购物品") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showSuggestions = true
                    },
                    label = { Text("食材名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Suggestions
                if (showSuggestions && suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column {
                            suggestions.forEach { ing ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            name = ing.name
                                            if (unit.isBlank()) unit = ing.unit
                                            showSuggestions = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(ing.name, style = MaterialTheme.typography.bodyMedium)
                                        Text("${ing.unit} · ${ing.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("数量") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("单位") },
                        placeholder = { Text("个/克/袋") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick add common ingredients
                if (name.isBlank()) {
                    val commonIngredients = knownIngredients.take(8)
                    if (commonIngredients.isNotEmpty()) {
                        Text("快速添加:", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            commonIngredients.forEach { ing ->
                                androidx.compose.material3.FilterChip(
                                    selected = false,
                                    onClick = { name = ing.name; if (unit.isBlank()) unit = ing.unit },
                                    label = { Text(ing.name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), quantity.trim(), unit.trim()) },
                enabled = name.isNotBlank()
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun EditShoppingItemDialog(
    item: ShoppingItemEntity,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(item.ingredientName) }
    var quantity by remember { mutableStateOf(item.quantity) }
    var unit by remember { mutableStateOf(item.unit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑采购物品") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("食材名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("数量") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("单位") },
                        placeholder = { Text("个/克/袋") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), quantity.trim(), unit.trim()) },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToInventoryDialog(
    itemName: String,
    defaultQuantity: String,
    defaultUnit: String,
    onConfirm: (Double, String, String, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf(defaultQuantity) }
    var unit by remember { mutableStateOf(defaultUnit) }
    var storageLocation by remember { mutableStateOf("冰箱冷藏") }
    var expireDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加到库存") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("将「$itemName」添加到库存", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it
                    }, label = { Text("数量") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f))
                    OutlinedTextField(value = unit, onValueChange = { unit = it },
                        label = { Text("单位") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = storageLocation, onValueChange = { storageLocation = it },
                    label = { Text("存放位置") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expireDate?.let { dateFormat.format(java.util.Date(it)) } ?: "未设置",
                    onValueChange = {}, label = { Text("过期日期") }, readOnly = true,
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(if (expireDate == null) "设置" else "修改")
                        }
                    })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val qty = quantity.toDoubleOrNull()
                if (qty != null && qty > 0) {
                    onConfirm(qty, storageLocation, unit, expireDate)
                }
            }, enabled = quantity.toDoubleOrNull() != null) {
                Text("添加到库存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("跳过") }
        }
    )

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = expireDate ?: System.currentTimeMillis()
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { expireDate = datePickerState.selectedDateMillis; showDatePicker = false }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}
