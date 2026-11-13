package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Customer
import com.example.model.Order
import com.example.model.OrderLineItem
import com.example.model.Product
import com.example.model.WestAfricaPaymentMethod
import com.example.ui.components.CurrencySelectorBar
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PaymentMethodBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatShortTime
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedStatusLabel
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProError
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersScreen(
    currency: String,
    acceptedPaymentMethods: List<String>,
    mobileMoneyNumber: String,
    orders: List<Order>,
    customers: List<Customer>,
    products: List<Product>,
    onCreateOrder: (
        customer: Customer,
        items: List<OrderLineItem>,
        notes: String,
        paymentMethod: String,
        paymentStatus: String,
        paymentReference: String
    ) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onConfirmPayment: (String) -> Unit,
    onSelectCurrency: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var statusFilter by remember { mutableStateOf("all") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filtered = remember(orders, statusFilter) {
        if (statusFilter == "all") orders else orders.filter { it.status == statusFilter }
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
                Text(strings.ordersTitle, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = strings.ordersSubtitleFormat(
                        orders.size,
                        formatCurrency(orders.filter { it.status != "cancelled" }.sumOf { it.totalAmount }, currency)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { showCreateDialog = true },
                enabled = customers.isNotEmpty() && products.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                modifier = Modifier.testTag("create_order_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.ordersNewOrder)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        CurrencySelectorBar(
            selectedCurrency = currency,
            onSelectCurrency = onSelectCurrency
        )

        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all", "pending", "confirmed", "preparing", "shipped", "delivered", "cancelled").forEach { st ->
                FilterChip(
                    selected = statusFilter == st,
                    onClick = { statusFilter = st },
                    label = { Text(localizedStatusLabel(st)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsProMintContainer,
                        selectedLabelColor = WhatsProOnMint
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard(
                title = strings.ordersEmptyTitle,
                subtitle = strings.ordersEmptySubtitle,
                icon = Icons.Rounded.ShoppingBag
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        currency = currency,
                        onUpdateStatus = { next -> onUpdateStatus(order.id, next) },
                        onConfirmPayment = { onConfirmPayment(order.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateOrderDialog(
            currency = currency,
            acceptedPaymentMethods = acceptedPaymentMethods,
            mobileMoneyNumber = mobileMoneyNumber,
            customers = customers,
            products = products,
            onDismiss = { showCreateDialog = false },
            onConfirm = { cust, items, notes, paymentMethod, paymentStatus, paymentRef ->
                onCreateOrder(cust, items, notes, paymentMethod, paymentStatus, paymentRef)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderCard(
    order: Order,
    currency: String,
    onUpdateStatus: (String) -> Unit,
    onConfirmPayment: () -> Unit
) {
    val strings = LocalAppStrings.current
    val nextStatuses = when (order.status) {
        "pending" -> listOf("confirmed", "cancelled")
        "confirmed" -> listOf("preparing", "cancelled")
        "preparing" -> listOf("shipped", "cancelled")
        "shipped" -> listOf("delivered")
        else -> emptyList()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.orderNumber} • ${order.customerName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${order.customerPhone} • ${formatShortTime(order.createdAt, strings.justNow)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusPill(
                    label = localizedStatusLabel(order.status),
                    type = order.status
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            PaymentMethodBadge(
                methodId = order.paymentMethod,
                paymentStatus = order.paymentStatus
            )
            if (order.paymentReference.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${strings.paymentRefPrefix} ${order.paymentReference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = WhatsProBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                order.items.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${line.quantity}x ${line.productName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatCurrency(line.quantity * line.unitPrice, currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${strings.ordersNotePrefix} ${order.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.ordersTotalLabel} ${formatCurrency(order.totalAmount, currency)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = WhatsProEmerald,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.paymentStatus != "paid" && order.status != "cancelled") {
                        OutlinedButton(
                            onClick = onConfirmPayment,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("confirm_payment_button_${order.id}")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = WhatsProEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.ordersConfirmPaymentBtn, color = WhatsProEmerald)
                        }
                    }
                    nextStatuses.forEach { st ->
                        if (st == "cancelled") {
                            TextButton(onClick = { onUpdateStatus(st) }) {
                                Text(strings.ordersCancelBtn, color = WhatsProError)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onUpdateStatus(st) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("${strings.ordersMarkPrefix} ${localizedStatusLabel(st)}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateOrderDialog(
    currency: String,
    acceptedPaymentMethods: List<String>,
    mobileMoneyNumber: String,
    customers: List<Customer>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (Customer, List<OrderLineItem>, String, String, String, String) -> Unit
) {
    val strings = LocalAppStrings.current
    val availableMethods = remember(acceptedPaymentMethods) {
        val list = acceptedPaymentMethods
            .map { WestAfricaPaymentMethod.fromId(it) }
            .distinct()
        list.ifEmpty { WestAfricaPaymentMethod.entries.toList() }
    }

    var selectedCustomer by remember { mutableStateOf(customers.first()) }
    val quantities = remember { mutableStateMapOf<String, Long>() }
    var selectedPayment by remember { mutableStateOf(availableMethods.first().id) }
    var paymentRef by remember { mutableStateOf("") }
    var isPaidNow by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val lineItems = products.mapNotNull { prod ->
        val qty = quantities[prod.id] ?: 0L
        if (qty > 0L) OrderLineItem(prod.id, prod.name, qty, prod.price) else null
    }
    val total = lineItems.sumOf { it.quantity * it.unitPrice }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.ordersDialogTitle) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(strings.ordersSelectCustomer, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    customers.take(8).forEach { c ->
                        FilterChip(
                            selected = selectedCustomer.id == c.id,
                            onClick = { selectedCustomer = c },
                            label = { Text(c.name) }
                        )
                    }
                }
                HorizontalDivider(color = WhatsProBorder)
                Text(strings.ordersSelectProducts, style = MaterialTheme.typography.labelLarge)
                products.filter { it.active }.forEach { prod ->
                    val qty = quantities[prod.id] ?: 0L
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatCurrency(prod.price, currency),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (qty > 0L) quantities[prod.id] = qty - 1L }
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "-")
                            }
                            Text("$qty", style = MaterialTheme.typography.titleSmall)
                            IconButton(
                                onClick = { quantities[prod.id] = qty + 1L }
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "+")
                            }
                        }
                    }
                }

                HorizontalDivider(color = WhatsProBorder)
                Text(strings.ordersPaymentMethodLabel, style = MaterialTheme.typography.labelLarge)
                if (mobileMoneyNumber.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WhatsProElectricBlueContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = WhatsProElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.ordersMerchantNumberHintPrefix} $mobileMoneyNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = WhatsProOnElectricBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableMethods.forEach { method ->
                        val accent = Color(method.accentColorHex)
                        FilterChip(
                            selected = selectedPayment == method.id,
                            onClick = { selectedPayment = method.id },
                            label = { Text(method.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("order_payment_chip_${method.id}")
                        )
                    }
                }

                if (selectedPayment != WestAfricaPaymentMethod.CASH_ON_DELIVERY.id) {
                    OutlinedTextField(
                        value = paymentRef,
                        onValueChange = { paymentRef = it },
                        label = { Text(strings.ordersPaymentRefLabel) },
                        placeholder = { Text("Ex: WV-88412 / OM-9032") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.ordersMarkPaidNowLabel, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isPaidNow,
                        onCheckedChange = { isPaidNow = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = WhatsProEmerald)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.ordersNotesLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${strings.ordersTotalLabel} ${formatCurrency(total, currency)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = WhatsProEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedCustomer,
                        lineItems,
                        notes,
                        selectedPayment,
                        if (isPaidNow) "paid" else "pending",
                        paymentRef.trim()
                    )
                },
                enabled = lineItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                modifier = Modifier.testTag("confirm_create_order_button")
            ) {
                Text(strings.ordersPlaceOrderBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
