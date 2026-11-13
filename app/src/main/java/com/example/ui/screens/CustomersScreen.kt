package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Customer
import com.example.model.CustomerSortOption
import com.example.model.RecordSearchFilter
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SearchWithFilterBar
import com.example.ui.components.StatusPill
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedStatusLabel
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProEmeraldDark
import com.example.ui.theme.WhatsProError
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomersScreen(
    currency: String,
    customers: List<Customer>,
    onSaveCustomer: (Customer) -> Unit,
    onDeleteCustomer: (String) -> Unit,
    onOpenChat: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }
    var segmentFilter by remember { mutableStateOf("all") }
    var sortBy by remember { mutableStateOf(CustomerSortOption.NAME_ASC) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }

    val activeFilterCount = remember(segmentFilter, sortBy) {
        var count = 0
        if (segmentFilter != "all") count++
        if (sortBy != CustomerSortOption.NAME_ASC) count++
        count
    }

    val filtered = remember(customers, searchQuery, segmentFilter, sortBy) {
        RecordSearchFilter.filterCustomers(
            customers = customers,
            query = searchQuery,
            segmentFilter = segmentFilter,
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
                Text(strings.customersTitle, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = strings.customersSubtitleFormat(customers.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { editingCustomer = Customer() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                modifier = Modifier.testTag("add_customer_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.customersAdd)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SearchWithFilterBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = strings.customersSearchPlaceholder,
            filtersExpanded = filtersExpanded,
            onToggleFilters = { filtersExpanded = !filtersExpanded },
            activeFilterCount = activeFilterCount,
            resultCount = filtered.size,
            onResetAll = {
                searchQuery = ""
                segmentFilter = "all"
                sortBy = CustomerSortOption.NAME_ASC
            },
            searchInputTestTag = "customer_search_input",
            filterButtonTestTag = "customer_filter_button",
            clearButtonTestTag = "customer_clear_search_button"
        ) {
            Text(
                text = strings.filterSegmentTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("all", "vip", "wholesale", "regular", "lead").forEach { key ->
                    FilterChip(
                        selected = segmentFilter == key,
                        onClick = { segmentFilter = key },
                        label = { Text(localizedStatusLabel(key)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsProMintContainer,
                            selectedLabelColor = WhatsProOnMint
                        ),
                        modifier = Modifier.testTag("customer_segment_chip_$key")
                    )
                }
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
                    selected = sortBy == CustomerSortOption.NAME_ASC,
                    onClick = { sortBy = CustomerSortOption.NAME_ASC },
                    label = { Text(strings.filterSortNameAsc) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsProElectricBlueContainer,
                        selectedLabelColor = WhatsProOnElectricBlue
                    )
                )
                FilterChip(
                    selected = sortBy == CustomerSortOption.SPENT_DESC,
                    onClick = { sortBy = CustomerSortOption.SPENT_DESC },
                    label = { Text(strings.filterSortSpentDesc) },
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
                title = strings.customersEmptyTitle,
                subtitle = strings.customersEmptySubtitle,
                icon = Icons.Rounded.Groups
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { cust ->
                    CustomerCard(
                        customer = cust,
                        currency = currency,
                        onEdit = { editingCustomer = cust },
                        onDelete = { onDeleteCustomer(cust.id) },
                        onOpenChat = { onOpenChat(cust) }
                    )
                }
            }
        }
    }

    editingCustomer?.let { target ->
        CustomerEditorDialog(
            initial = target,
            onDismiss = { editingCustomer = null },
            onConfirm = { updated ->
                onSaveCustomer(updated)
                editingCustomer = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerCard(
    customer: Customer,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenChat: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WhatsProMintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = WhatsProEmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        StatusPill(
                            label = localizedStatusLabel(customer.segment),
                            type = customer.segment
                        )
                    }
                    Text(
                        text = listOf(customer.phone, customer.email, customer.company)
                            .filter { it.isNotBlank() }
                            .joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = customer.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (customer.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    customer.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.customersOrdersSpentFormat(
                        customer.totalOrders,
                        formatCurrency(customer.totalSpent, currency)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onOpenChat,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.customersChatBtn)
                    }
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
private fun CustomerEditorDialog(
    initial: Customer,
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial.name) }
    var phone by remember { mutableStateOf(initial.phone) }
    var email by remember { mutableStateOf(initial.email) }
    var company by remember { mutableStateOf(initial.company) }
    var segment by remember { mutableStateOf(initial.segment.ifBlank { "regular" }) }
    var tagsText by remember { mutableStateOf(initial.tags.joinToString(", ")) }
    var notes by remember { mutableStateOf(initial.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id.isBlank()) strings.customersAddTitle else strings.customersEditTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.customersNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.customersPhoneLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_phone_input")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(strings.customersEmailLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text(strings.customersCompanyLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("lead", "regular", "vip", "wholesale").forEach { seg ->
                        FilterChip(
                            selected = segment == seg,
                            onClick = { segment = seg },
                            label = { Text(localizedStatusLabel(seg)) }
                        )
                    }
                }
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text(strings.customersTagsLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.customersNotesLabel) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(10)
                    onConfirm(
                        initial.copy(
                            name = name,
                            phone = phone,
                            email = email,
                            company = company,
                            segment = segment,
                            tags = tags,
                            notes = notes
                        )
                    )
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                modifier = Modifier.testTag("save_customer_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
