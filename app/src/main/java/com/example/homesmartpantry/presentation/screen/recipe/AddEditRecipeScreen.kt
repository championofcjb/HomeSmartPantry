package com.example.homesmartpantry.presentation.screen.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.homesmartpantry.domain.model.RecipeIngredient
import com.example.homesmartpantry.domain.model.RecipeStep
import kotlinx.coroutines.launch

data class RecipeFormIngredient(
    val name: String, val quantity: String = "", val unit: String = "",
    val inStock: Boolean = false, val stockQty: Double? = null, val stockUnit: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditRecipeScreen(
    viewModel: RecipeViewModel,
    editRecipeId: Long? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var recipeCategory by remember { mutableStateOf("家常菜") }
    var difficulty by remember { mutableStateOf("普通") }
    var cookTime by remember { mutableStateOf("30分钟") }
    var servings by remember { mutableStateOf("2人份") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<RecipeFormIngredient>() }
    val steps = remember { mutableStateListOf<String>() }
    val tags = remember { mutableStateListOf<String>() }
    var newTag by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    var showAddIngredient by remember { mutableStateOf(false) }
    var newIngName by remember { mutableStateOf("") }
    var newIngQty by remember { mutableStateOf("") }
    var newIngUnit by remember { mutableStateOf("") }
    var showAddStep by remember { mutableStateOf(false) }
    var newStepText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var duplicateError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load known ingredients when dialog opens
    val knownIngs by viewModel.knownIngredients.collectAsState()
    LaunchedEffect(showAddIngredient) { if (showAddIngredient) viewModel.loadKnownIngredients() }
    val filteredKnown = knownIngs.filter { it.name.contains(newIngName, ignoreCase = true) }

    // Load edit data
    val editDataState by viewModel.editData.collectAsState()
    LaunchedEffect(editRecipeId) {
        if (editRecipeId != null && !initialized) {
            viewModel.loadEditData(editRecipeId)
        }
    }
    LaunchedEffect(editDataState) {
        val d = editDataState ?: return@LaunchedEffect
        if (editRecipeId != null && !initialized) {
            name = d.recipe.name
            description = d.recipe.description
            recipeCategory = d.recipe.category
            difficulty = d.recipe.difficulty
            cookTime = d.recipe.cookTime
            servings = d.recipe.servings
            calories = d.recipe.calories
            protein = d.recipe.protein
            fat = d.recipe.fat
            notes = d.recipe.notes
            d.ingredients.forEach { ingredients.add(RecipeFormIngredient(it.ingredientName, it.quantity, it.unit)) }
            d.steps.forEach { steps.add(it.description) }
            d.tags.forEach { tags.add(it) }
            initialized = true
        }
    }

    fun save() {
        if (name.isBlank()) { scope.launch { snackbarHostState.showSnackbar("请输入菜谱名称") }; return }
        if (ingredients.isEmpty()) { scope.launch { snackbarHostState.showSnackbar("请至少添加一种食材") }; return }
        isSaving = true
        viewModel.saveRecipe(
            id = editRecipeId, name = name.trim(), description = description.trim(),
            category = recipeCategory, difficulty = difficulty, cookTime = cookTime,
            servings = servings, calories = calories, protein = protein, fat = fat, notes = notes.trim(),
            ingredients = ingredients.map { RecipeIngredient(0, it.name, it.quantity.ifBlank { "适量" }, it.unit) },
            steps = steps.mapIndexed { i, s -> RecipeStep(recipeId = 0, stepNumber = i + 1, description = s) },
            tags = tags.toList(),
            onDone = { isSaving = false; onBack() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editRecipeId != null) "编辑菜谱" else "添加菜谱") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            item {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("菜谱名称") }, placeholder = { Text("例如: 番茄炒蛋") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item { ChipSelector("分类", categories, recipeCategory) { recipeCategory = it } }
            item { ChipSelector("难度", difficulties, difficulty) { difficulty = it } }
            item { ChipSelector("烹饪时间", cookTimes, cookTime) { cookTime = it } }
            item { ChipSelector("份量", servingOptions, servings) { servings = it } }
            item {
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("做法描述") }, placeholder = { Text("简要描述烹饪方法...") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
            item { SectionLabel("食材清单") }
            items(ingredients) { ing ->
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp), shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ing.name, style = MaterialTheme.typography.bodyLarge)
                            Text("${ing.quantity.ifBlank { "适量" }} ${ing.unit}",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { ingredients.remove(ing) }) {
                            Icon(Icons.Default.Close, "移除", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            item {
                TextButton(onClick = { showAddIngredient = true }) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("添加食材")
                }
            }
            item { SectionLabel("制作步骤") }
            itemsIndexed(steps) { i, step ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.width(24.dp))
                    Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { steps.removeAt(i) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
            item {
                TextButton(onClick = { showAddStep = true }) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("添加步骤")
                }
            }
            item { SectionLabel("标签") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        FilterChip(selected = true, onClick = { tags.remove(tag) }, label = { Text(tag, style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = { Icon(Icons.Default.Close, "删除", Modifier.size(16.dp)) })
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = newTag, onValueChange = { newTag = it },
                        placeholder = { Text("输入标签") }, modifier = Modifier.weight(1f), singleLine = true)
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { if (newTag.isNotBlank() && newTag !in tags) { tags.add(newTag.trim()); newTag = "" } },
                        enabled = newTag.isNotBlank(), shape = MaterialTheme.shapes.small) { Text("添加") }
                }
            }
            item { SectionLabel("营养信息（选填）") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = calories, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) calories = it },
                        label = { Text("热量") }, suffix = { Text("kcal") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = protein, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) protein = it },
                        label = { Text("蛋白质") }, suffix = { Text("g") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = fat, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) fat = it },
                        label = { Text("脂肪") }, suffix = { Text("g") }, modifier = Modifier.weight(1f), singleLine = true)
                }
            }
            item {
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("备注（选填）") }, placeholder = { Text("适合几人食用、注意事项...") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { save() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isSaving, shape = MaterialTheme.shapes.small) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    else Text("保存菜谱")
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Add ingredient dialog
    if (showAddIngredient) {
        AlertDialog(
            onDismissRequest = { showAddIngredient = false; newIngName = ""; newIngQty = ""; newIngUnit = ""; duplicateError = false },
            title = { Text("添加食材") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newIngName, onValueChange = { newIngName = it; duplicateError = ingredients.any { ing -> ing.name.equals(it, true) } },
                        label = { Text("搜索或输入食材名称") }, isError = duplicateError,
                        supportingText = if (duplicateError) {{ Text("该食材已添加", color = MaterialTheme.colorScheme.error) }} else null,
                        singleLine = true, modifier = Modifier.fillMaxWidth())

                    // Existing ingredients picker
                    if (filteredKnown.isNotEmpty() && newIngName.isNotBlank()) {
                        LazyColumn(modifier = Modifier.weight(1f, fill = false).fillMaxWidth().height(200.dp)) {
                            items(filteredKnown.take(8)) { known ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { newIngName = known.name; newIngUnit = known.unit }
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(known.name, style = MaterialTheme.typography.bodyMedium)
                                        Text(known.category, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (known.stockQty != null) {
                                        Text("库存 ${formatStock(known.stockQty)} ${known.stockUnit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(known.unit, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = newIngQty, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) newIngQty = it },
                            label = { Text("用量") }, modifier = Modifier.weight(1f), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(value = newIngUnit, onValueChange = { newIngUnit = it },
                            label = { Text("单位") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    ingredients.add(RecipeFormIngredient(newIngName.trim(), newIngQty, newIngUnit.trim()))
                    newIngName = ""; newIngQty = ""; newIngUnit = ""; duplicateError = false; showAddIngredient = false
                }, enabled = newIngName.isNotBlank() && !duplicateError) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddIngredient = false; newIngName = ""; newIngQty = ""; newIngUnit = ""; duplicateError = false }) { Text("取消") } }
        )
    }

    // Add step dialog
    if (showAddStep) {
        AlertDialog(
            onDismissRequest = { showAddStep = false; newStepText = "" },
            title = { Text("步骤 ${steps.size + 1}") },
            text = {
                OutlinedTextField(value = newStepText, onValueChange = { newStepText = it },
                    label = { Text("步骤描述") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newStepText.isNotBlank()) { steps.add(newStepText.trim()); newStepText = ""; showAddStep = false }
                }, enabled = newStepText.isNotBlank()) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddStep = false; newStepText = "" }) { Text("取消") } }
        )
    }
}

private val categories = listOf("家常菜", "快手菜", "早餐", "晚餐", "面食", "甜品", "汤类")
private val difficulties = listOf("简单", "普通", "困难")
private val cookTimes = listOf("15分钟", "30分钟", "60分钟")
private val servingOptions = listOf("1人份", "2人份", "4人份")

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSelector(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { opt ->
                FilterChip(selected = selected == opt, onClick = { onSelect(opt) },
                    label = { Text(opt, style = MaterialTheme.typography.bodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

private fun formatStock(qty: Double): String {
    return if (qty == qty.toLong().toDouble()) qty.toLong().toString()
    else String.format("%.1f", qty)
}
