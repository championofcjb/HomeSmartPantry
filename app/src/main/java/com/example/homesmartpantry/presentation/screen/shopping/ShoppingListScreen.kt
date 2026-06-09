package com.example.homesmartpantry.presentation.screen.shopping

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.homesmartpantry.data.local.entity.ShoppingItemEntity
import com.example.homesmartpantry.data.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    shoppingItems: Flow<List<ShoppingItemEntity>>,
    onMarkPurchased: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onClearPurchased: () -> Unit,
    onBack: () -> Unit
) {
    val items by shoppingItems.collectAsState(initial = emptyList())
    val unpurchased = items.filter { !it.isPurchased }
    val purchased = items.filter { it.isPurchased }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("采购清单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回"
                        )
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
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("采购清单为空\n可从菜谱详情页添加", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
                        onToggle = { onMarkPurchased(item.id) },
                        onDelete = { onDelete(item.id) }
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
                        onToggle = { onMarkPurchased(item.id) },
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
