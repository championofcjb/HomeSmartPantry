package com.example.homesmartpantry.presentation.screen.ingredient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    viewModel: AddIngredientViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reset form on every entry
    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("添加成功")
            onBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加物品") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Category selector
            item {
                CategorySelector(
                    category = uiState.category,
                    onCategoryChange = viewModel::updateCategory
                )
            }

            // Name with autocomplete
            item {
                IngredientNameField(
                    name = uiState.name,
                    onNameChange = viewModel::updateName,
                    searchResults = uiState.searchResults,
                    onSelect = viewModel::selectIngredient
                )
            }

            // Quantity
            item {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = viewModel::updateQuantity,
                    label = { Text("数量") },
                    placeholder = { Text("例如: 500") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // Unit
            item {
                UnitSelector(
                    unit = uiState.unit,
                    onUnitChange = viewModel::updateUnit
                )
            }

            // Shelf Life
            item {
                OutlinedTextField(
                    value = uiState.shelfLifeDays,
                    onValueChange = viewModel::updateShelfLifeDays,
                    label = { Text("保质期") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = {
                        if (uiState.shelfLifeDays.isNotBlank()) {
                            Text(
                                text = "天",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    supportingText = {
                        if (uiState.shelfLifeDays.isNotBlank()) {
                            val days = uiState.shelfLifeDays.toIntOrNull()
                            if (days != null && days > 0) {
                                val javaDateFormat = java.text.SimpleDateFormat(
                                    "MM/dd", java.util.Locale.getDefault()
                                )
                                val expireDate = java.util.Date(
                                    System.currentTimeMillis() + days * 24L * 60 * 60 * 1000
                                )
                                Text(
                                    text = "约 ${javaDateFormat.format(expireDate)} 过期",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }

            // Storage Location
            item {
                StorageLocationSelector(
                    location = uiState.storageLocation,
                    onLocationChange = viewModel::updateStorageLocation
                )
            }

            // Price
            item {
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = viewModel::updatePrice,
                    label = { Text("花费价格") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("¥", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
            }

            // Image placeholder
            item {
                ImagePlaceholder()
            }

            // Save button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isSaving,
                    shape = MaterialTheme.shapes.small
                ) {
                    if (uiState.isSaving) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("保存", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    category: String,
    onCategoryChange: (String) -> Unit
) {
    val categories = listOf("食材", "调味料", "主食粮油")

    Column {
        Text(
            text = "分类",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat, style = MaterialTheme.typography.bodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun IngredientNameField(
    name: String,
    onNameChange: (String) -> Unit,
    searchResults: List<com.example.homesmartpantry.domain.model.Ingredient>,
    onSelect: (com.example.homesmartpantry.domain.model.Ingredient) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("名称") },
            placeholder = { Text("例如: 西红柿、酱油、大米...") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            singleLine = true,
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { onNameChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            }
        )

        // Autocomplete dropdown
        if (isFocused && searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    searchResults.take(6).forEach { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(ingredient) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = ingredient.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = ingredient.unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnitSelector(
    unit: String,
    onUnitChange: (String) -> Unit
) {
    val commonUnits = listOf("g", "ml", "个", "斤", "瓶", "袋", "盒", "包", "片", "只", "根", "颗")

    Column {
        OutlinedTextField(
            value = unit,
            onValueChange = onUnitChange,
            label = { Text("单位") },
            placeholder = { Text("选择或输入单位") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonUnits.forEach { commonUnit ->
                FilterChip(
                    selected = unit == commonUnit,
                    onClick = {
                        if (unit == commonUnit) {
                            onUnitChange("")
                        } else {
                            onUnitChange(commonUnit)
                        }
                    },
                    label = {
                        Text(
                            text = commonUnit,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StorageLocationSelector(
    location: String,
    onLocationChange: (String) -> Unit
) {
    val locations = listOf("冰箱冷藏", "冰箱冷冻", "常温储藏")

    Column {
        Text(
            text = "存放位置",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            locations.forEach { loc ->
                FilterChip(
                    selected = location == loc,
                    onClick = { onLocationChange(loc) },
                    label = { Text(loc, style = MaterialTheme.typography.bodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun ImagePlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加图片",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "添加图片",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
