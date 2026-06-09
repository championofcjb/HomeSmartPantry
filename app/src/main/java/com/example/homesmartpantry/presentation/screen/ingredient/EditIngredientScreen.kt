package com.example.homesmartpantry.presentation.screen.ingredient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.homesmartpantry.domain.model.InventoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientScreen(
    item: InventoryItem?,
    onBack: () -> Unit,
    onSave: (Double, String, Double?, Long?) -> Unit,
    onDelete: () -> Unit
) {
    if (item == null) {
        // Item not loaded yet or not found
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("编辑食材") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    "食材信息加载中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var quantity by remember { mutableStateOf(formatQty(item.quantity)) }
    var storageLocation by remember { mutableStateOf(item.storageLocation) }
    var price by remember { mutableStateOf(item.price?.let { formatQty(it) } ?: "") }
    var expireDate by remember { mutableStateOf(item.expireDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDateClearConfirm by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑 ${item.ingredientName}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Name (read-only)
            OutlinedTextField(
                value = item.ingredientName,
                onValueChange = {},
                label = { Text("食材名称") },
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Category (read-only)
            OutlinedTextField(
                value = item.category,
                onValueChange = {},
                label = { Text("分类") },
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Quantity
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it
                },
                label = { Text("数量") },
                suffix = { Text(item.unit) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Storage location
            OutlinedTextField(
                value = storageLocation,
                onValueChange = { storageLocation = it },
                label = { Text("存放位置") },
                placeholder = { Text("如：冰箱冷藏、冷冻室、橱柜") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) price = it
                },
                label = { Text("价格") },
                prefix = { Text("¥") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Expire date
            OutlinedTextField(
                value = expireDate?.let { dateFormat.format(Date(it)) } ?: "未设置",
                onValueChange = {},
                label = { Text("过期日期") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(if (expireDate == null) "设置" else "修改")
                    }
                },
                supportingText = if (expireDate != null) {
                    {
                        TextButton(onClick = { showDateClearConfirm = true }) {
                            Text("清除日期", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else null
            )

            // Info text
            Text(
                text = "购买日期: ${item.purchaseDate?.let { dateFormat.format(Date(it)) } ?: "未记录"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val qty = quantity.toDoubleOrNull()
                    if (qty != null && qty >= 0) {
                        onSave(qty, storageLocation, price.toDoubleOrNull(), expireDate)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = quantity.toDoubleOrNull() != null
            ) {
                Text("保存修改")
            }

            // Delete button
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("删除此食材")
            }

            Spacer(Modifier.height(24.dp))
        }

        // Date picker dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = expireDate ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        expireDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Delete confirmation dialog
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("确认删除") },
                text = { Text("确定要删除「${item.ingredientName}」吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // Clear date confirmation
        if (showDateClearConfirm) {
            AlertDialog(
                onDismissRequest = { showDateClearConfirm = false },
                title = { Text("清除过期日期") },
                text = { Text("确定要清除过期日期吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        expireDate = null
                        showDateClearConfirm = false
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDateClearConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

private fun formatQty(qty: Double): String {
    return if (qty == qty.toLong().toDouble()) qty.toLong().toString()
    else String.format("%.1f", qty)
}
