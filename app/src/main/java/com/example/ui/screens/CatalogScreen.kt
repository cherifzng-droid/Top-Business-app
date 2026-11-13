package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Product
import com.example.model.ProductSortOption
import com.example.model.RecordSearchFilter
import com.example.ui.components.CurrencySelectorBar
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.SearchWithFilterBar
import com.example.ui.components.StatusPill
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProError
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private suspend fun persistLocalProductImage(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "product_images")
            if (!dir.exists()) dir.mkdirs()
            val target = File(dir, "prod_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            target.absolutePath
        } catch (_: Exception) {
            null
        }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    currency: String,
    products: List<Product>,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onSelectCurrency: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("__ALL__") }
    var onlyActive by remember { mutableStateOf(false) }
    var lowStockOnly by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(ProductSortOption.NAME_ASC) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val categories = remember(products) {
        listOf("__ALL__") + products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val activeFilterCount = remember(selectedCategory, onlyActive, lowStockOnly, sortBy) {
        var count = 0
        if (selectedCategory != "__ALL__") count++
        if (onlyActive) count++
        if (lowStockOnly) count++
        if (sortBy != ProductSortOption.NAME_ASC) count++
        count
    }

    val filtered = remember(products, searchQuery, selectedCategory, onlyActive, lowStockOnly, sortBy) {
        RecordSearchFilter.filterProducts(
            products = products,
            query = searchQuery,
            selectedCategory = selectedCategory,
            onlyActive = onlyActive,
            lowStockOnly = lowStockOnly,
            sortBy = sortBy
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(strings.catalogTitle, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = strings.catalogSubtitleFormat(products.count { it.active }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { editingProduct = Product(category = strings.catalogCategoryLabel) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                modifier = Modifier.testTag("add_product_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.catalogAddProduct)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        CurrencySelectorBar(
            selectedCurrency = currency,
            onSelectCurrency = onSelectCurrency
        )

        Spacer(modifier = Modifier.height(10.dp))
        SearchWithFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = strings.catalogSearchPlaceholder,
            filtersExpanded = filtersExpanded,
            onToggleFilters = { filtersExpanded = !filtersExpanded },
            activeFilterCount = activeFilterCount,
            resultCount = filtered.size,
            onResetAll = {
                searchQuery = ""
                selectedCategory = "__ALL__"
                onlyActive = false
                lowStockOnly = false
                sortBy = ProductSortOption.NAME_ASC
            },
            searchInputTestTag = "product_search_input",
            filterButtonTestTag = "product_filter_button",
            clearButtonTestTag = "product_clear_search_button"
        ) {
            Text(
                text = strings.filterCategoryTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(if (cat == "__ALL__") strings.catalogAllCategory else cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsProMintContainer,
                            selectedLabelColor = WhatsProOnMint
                        )
                    )
                }
            }

            Text(
                text = strings.filterAvailabilityTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = onlyActive,
                    onClick = { onlyActive = !onlyActive },
                    label = { Text(strings.filterOnlyActive) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsProMintContainer,
                        selectedLabelColor = WhatsProOnMint
                    ),
                    modifier = Modifier.testTag("product_filter_active_chip")
                )
                FilterChip(
                    selected = lowStockOnly,
                    onClick = { lowStockOnly = !lowStockOnly },
                    label = { Text(strings.filterLowStock) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsProElectricBlueContainer,
                        selectedLabelColor = WhatsProOnElectricBlue
                    ),
                    modifier = Modifier.testTag("product_filter_low_stock_chip")
                )
            }

            Text(
                text = strings.filterSortTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = sortBy == ProductSortOption.NAME_ASC,
                    onClick = { sortBy = ProductSortOption.NAME_ASC },
                    label = { Text(strings.filterSortNameAsc) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsProElectricBlueContainer,
                        selectedLabelColor = WhatsProOnElectricBlue
                    )
                )
                FilterChip(
                    selected = sortBy == ProductSortOption.PRICE_ASC,
                    onClick = { sortBy = ProductSortOption.PRICE_ASC },
                    label = { Text(strings.filterSortPriceAsc) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsProElectricBlueContainer,
                        selectedLabelColor = WhatsProOnElectricBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard(
                title = strings.catalogEmptyTitle,
                subtitle = strings.catalogEmptySubtitle,
                icon = Icons.Rounded.Inventory2
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { prod ->
                    ProductCard(
                        product = prod,
                        currency = currency,
                        onToggleActive = { active -> onSaveProduct(prod.copy(active = active)) },
                        onEdit = { editingProduct = prod },
                        onDelete = { onDeleteProduct(prod.id) }
                    )
                }
            }
        }
    }

    editingProduct?.let { target ->
        ProductEditorDialog(
            initial = target,
            currency = currency,
            onDismiss = { editingProduct = null },
            onConfirm = { updated ->
                onSaveProduct(updated)
                editingProduct = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductCard(
    product: Product,
    currency: String,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val hasLocalCustomPhoto = product.localImagePath.isNotBlank() || product.imageUri.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductThumbnail(
                imageKey = product.imageKey,
                imageUri = product.imageUri,
                localImagePath = product.localImagePath,
                contentDescription = product.name,
                modifier = Modifier.size(86.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatCurrency(product.price, currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = WhatsProEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusPill(label = product.category, type = "regular")
                    StatusPill(
                        label = "${strings.catalogStockPrefix} ${product.stock}",
                        type = if (product.stock > 5) "active" else "pending"
                    )
                    if (hasLocalCustomPhoto) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WhatsProElectricBlueContainer
                        ) {
                            Text(
                                text = strings.catalogLocalPhotoBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = WhatsProOnElectricBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = product.active,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(checkedTrackColor = WhatsProEmerald)
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = strings.edit)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = WhatsProError)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductEditorDialog(
    initial: Product,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var category by remember { mutableStateOf(initial.category.ifBlank { "Général" }) }
    var sku by remember { mutableStateOf(initial.sku) }
    var priceText by remember {
        mutableStateOf(
            if (initial.price > 0) {
                if (initial.price % 1.0 == 0.0) initial.price.toLong().toString()
                else initial.price.toString()
            } else ""
        )
    }
    var stockText by remember { mutableStateOf(if (initial.stock > 0) initial.stock.toString() else "10") }
    var imageKey by remember { mutableStateOf(initial.imageKey.ifBlank { "coffee" }) }
    var imageUri by remember { mutableStateOf(initial.imageUri) }
    var localImagePath by remember { mutableStateOf(initial.localImagePath) }
    var isCopyingImage by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isCopyingImage = true
            scope.launch {
                val savedPath = persistLocalProductImage(context, uri)
                if (savedPath != null) {
                    localImagePath = savedPath
                    imageUri = "file://$savedPath"
                } else {
                    imageUri = uri.toString()
                }
                isCopyingImage = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial.id.isBlank()) strings.catalogAddProduct else strings.catalogEditTitle)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live local image preview row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, WhatsProBorder, RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ProductThumbnail(
                            imageKey = imageKey,
                            imageUri = imageUri,
                            localImagePath = localImagePath,
                            contentDescription = name.ifBlank { strings.catalogPhotoSectionLabel },
                            modifier = Modifier.size(64.dp)
                        )
                        if (isCopyingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = WhatsProElectricBlue,
                                strokeWidth = 2.5.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.catalogPhotoSectionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isCopyingImage) strings.catalogCopyingPhoto else strings.catalogLocalPhotoHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isCopyingImage,
                        modifier = Modifier.testTag("pick_local_photo_button")
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (localImagePath.isNotBlank() || imageUri.isNotBlank()) {
                                strings.catalogLocalPhotoPicked
                            } else {
                                strings.catalogPickLocalPhoto
                            }
                        )
                    }
                    if (localImagePath.isNotBlank() || imageUri.isNotBlank()) {
                        TextButton(
                            onClick = {
                                localImagePath = ""
                                imageUri = ""
                            }
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.catalogResetPhoto)
                        }
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "coffee" to strings.catalogPresetCoffee,
                        "bakery" to strings.catalogPresetBakery,
                        "gift" to strings.catalogPresetGift
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = localImagePath.isBlank() && imageUri.isBlank() && imageKey == key,
                            onClick = {
                                localImagePath = ""
                                imageUri = ""
                                imageKey = key
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsProMintContainer,
                                selectedLabelColor = WhatsProOnMint
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.catalogProductNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.catalogDescriptionLabel) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("${strings.catalogPriceLabelPrefix} ($currency) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_price_input")
                    )
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text(strings.catalogStockLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(strings.catalogCategoryLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text(strings.catalogSkuLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            val parsedPrice = priceText.replace(",", ".").toDoubleOrNull()
            val parsedStock = stockText.toLongOrNull() ?: 0L
            Button(
                onClick = {
                    onConfirm(
                        initial.copy(
                            name = name,
                            description = description,
                            category = category,
                            sku = sku,
                            price = parsedPrice ?: 0.0,
                            stock = parsedStock,
                            imageKey = imageKey,
                            imageUri = imageUri,
                            localImagePath = localImagePath
                        )
                    )
                },
                enabled = !isCopyingImage && name.isNotBlank() && parsedPrice != null && parsedPrice >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
