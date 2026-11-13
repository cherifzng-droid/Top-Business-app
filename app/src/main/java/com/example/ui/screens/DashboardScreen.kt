package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AutomationRule
import com.example.model.Conversation
import com.example.model.Customer
import com.example.model.Order
import com.example.model.WorkspaceProfile
import com.example.ui.WhatsProSection
import com.example.ui.components.KpiStatCard
import com.example.ui.components.PaymentMethodBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.WhatsAppFoundationBanner
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedStatusLabel
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProDeepBlack
import com.example.ui.theme.WhatsProDeepBlackMuted
import com.example.ui.theme.WhatsProDeepBlackSurface
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProEmeraldBright
import com.example.ui.theme.WhatsProEmeraldDark
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProNeoRed
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint
import com.example.ui.theme.WhatsProSuccess
import com.example.ui.theme.WhatsProSuccessContainer

@Composable
fun DashboardScreen(
    workspace: WorkspaceProfile,
    customers: List<Customer>,
    conversations: List<Conversation>,
    orders: List<Order>,
    automations: List<AutomationRule>,
    onNavigate: (WhatsProSection) -> Unit,
    onOpenConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val activeOrders = orders.filter { it.status != "cancelled" }
    val totalSales = activeOrders.sumOf { it.totalAmount }
    val avgOrderValue = if (activeOrders.isNotEmpty()) totalSales / activeOrders.size else 0.0
    val deliveredOrders = orders.count { it.status == "delivered" }
    val inProgressOrders = orders.count { it.status == "pending" || it.status == "confirmed" || it.status == "preparing" || it.status == "shipped" }
    val vipCustomers = customers.count { it.segment == "vip" }
    val totalAutomationHits = automations.sumOf { it.hitCount }
    val activeRulesCount = automations.count { it.active }
    val openConversations = conversations.count { it.status != "resolved" }
    val automatedShare = if (conversations.isNotEmpty()) {
        (conversations.count { it.status == "automated" || it.assignedRule.isNotBlank() } * 100) / conversations.size
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Branded Noir profond + Bleu électrique + Rouge néo Hero Command Banner
        DashboardHeroHeader(
            workspace = workspace,
            openConversations = openConversations,
            activeRulesCount = activeRulesCount
        )

        WhatsAppFoundationBanner()

        // Section Label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.dashSummaryTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = WhatsProDeepBlack,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.dashSummarySubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = WhatsProMintContainer
            ) {
                Text(
                    text = strings.dashGreenKpisBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = WhatsProOnMint,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Featured Green-Accented Summary Cards: Total Customers, Orders, Sales Stats (+ Conversations & Automations)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 820.dp
            if (isWide) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiStatCard(
                            title = strings.dashTotalCustomers,
                            value = "${customers.size}",
                            subtitle = "$vipCustomers VIP • ${customers.count { it.segment == "lead" }} ${localizedStatusLabel("lead")}",
                            icon = Icons.Rounded.Groups,
                            accentColor = WhatsProEmerald,
                            containerColor = WhatsProMintContainer,
                            badgeText = "CRM",
                            onClick = { onNavigate(WhatsProSection.CUSTOMERS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_kpi_customers")
                        )
                        KpiStatCard(
                            title = strings.dashTotalOrders,
                            value = "${orders.size}",
                            subtitle = "$inProgressOrders ${strings.dashActiveSuffix} • $deliveredOrders ${strings.dashDeliveredSuffix}",
                            icon = Icons.Rounded.ShoppingBag,
                            accentColor = WhatsProEmeraldDark,
                            containerColor = WhatsProSuccessContainer,
                            badgeText = "${activeOrders.size} ${strings.dashValidOrdersSuffix}",
                            onClick = { onNavigate(WhatsProSection.ORDERS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_kpi_orders")
                        )
                        SalesStatsGreenCard(
                            totalSales = formatCurrency(totalSales, workspace.currency),
                            avgOrderValue = formatCurrency(avgOrderValue, workspace.currency),
                            orderCount = activeOrders.size,
                            orders = activeOrders,
                            onClick = { onNavigate(WhatsProSection.ORDERS) },
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("dashboard_kpi_sales")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiStatCard(
                            title = strings.dashConversations,
                            value = "${conversations.size}",
                            subtitle = "$openConversations ${strings.dashActiveThreadsSuffix}",
                            icon = Icons.Rounded.ChatBubbleOutline,
                            accentColor = WhatsProEmerald,
                            containerColor = WhatsProMintContainer,
                            badgeText = strings.sectionInbox,
                            onClick = { onNavigate(WhatsProSection.INBOX) },
                            modifier = Modifier.weight(1f)
                        )
                        KpiStatCard(
                            title = strings.dashAutomationStats,
                            value = "$totalAutomationHits",
                            subtitle = "$activeRulesCount ${strings.dashRulesSuffix} • $automatedShare% ${strings.dashAutoSuffix}",
                            icon = Icons.Rounded.AutoMode,
                            accentColor = WhatsProSuccess,
                            containerColor = WhatsProSuccessContainer,
                            badgeText = strings.dashRulesSuffix,
                            onClick = { onNavigate(WhatsProSection.AUTOMATION) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Featured Green Sales Stats Hero Card
                    SalesStatsGreenCard(
                        totalSales = formatCurrency(totalSales, workspace.currency),
                        avgOrderValue = formatCurrency(avgOrderValue, workspace.currency),
                        orderCount = activeOrders.size,
                        orders = activeOrders,
                        onClick = { onNavigate(WhatsProSection.ORDERS) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_kpi_sales")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiStatCard(
                            title = strings.dashTotalCustomers,
                            value = "${customers.size}",
                            subtitle = "$vipCustomers VIP",
                            icon = Icons.Rounded.Groups,
                            accentColor = WhatsProEmerald,
                            containerColor = WhatsProMintContainer,
                            badgeText = "CRM",
                            onClick = { onNavigate(WhatsProSection.CUSTOMERS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_kpi_customers")
                        )
                        KpiStatCard(
                            title = strings.dashTotalOrders,
                            value = "${orders.size}",
                            subtitle = "$inProgressOrders ${strings.dashInProgressSuffix}",
                            icon = Icons.Rounded.ShoppingBag,
                            accentColor = WhatsProEmeraldDark,
                            containerColor = WhatsProSuccessContainer,
                            badgeText = "$deliveredOrders ${strings.dashDoneSuffix}",
                            onClick = { onNavigate(WhatsProSection.ORDERS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_kpi_orders")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiStatCard(
                            title = strings.dashConversations,
                            value = "${conversations.size}",
                            subtitle = "$openConversations ${strings.dashActiveThreadsSuffix}",
                            icon = Icons.Rounded.ChatBubbleOutline,
                            accentColor = WhatsProEmerald,
                            containerColor = WhatsProMintContainer,
                            onClick = { onNavigate(WhatsProSection.INBOX) },
                            modifier = Modifier.weight(1f)
                        )
                        KpiStatCard(
                            title = strings.dashAutomationHits,
                            value = "$totalAutomationHits",
                            subtitle = "$activeRulesCount ${strings.dashRulesSuffix} ($automatedShare% ${strings.dashAutoSuffix})",
                            icon = Icons.Rounded.AutoMode,
                            accentColor = WhatsProSuccess,
                            containerColor = WhatsProSuccessContainer,
                            onClick = { onNavigate(WhatsProSection.AUTOMATION) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Analytics Chart Card
        AutomationPerformanceChartCard(
            totalHits = totalAutomationHits,
            ordersCount = orders.size,
            automations = automations
        )

        // Two-column bottom summary on wide screens, stacked on mobile
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val sideBySide = maxWidth >= 760.dp
            if (sideBySide) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RecentInboxCard(
                        conversations = conversations.take(4),
                        onViewAll = { onNavigate(WhatsProSection.INBOX) },
                        onOpenConversation = onOpenConversation,
                        modifier = Modifier.weight(1f)
                    )
                    RecentOrdersCard(
                        orders = orders.take(4),
                        currency = workspace.currency,
                        onViewAll = { onNavigate(WhatsProSection.ORDERS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RecentInboxCard(
                        conversations = conversations.take(3),
                        onViewAll = { onNavigate(WhatsProSection.INBOX) },
                        onOpenConversation = onOpenConversation,
                        modifier = Modifier.fillMaxWidth()
                    )
                    RecentOrdersCard(
                        orders = orders.take(3),
                        currency = workspace.currency,
                        onViewAll = { onNavigate(WhatsProSection.ORDERS) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeroHeader(
    workspace: WorkspaceProfile,
    openConversations: Int,
    activeRulesCount: Int
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(WhatsProDeepBlack, WhatsProDeepBlackSurface, WhatsProDeepBlackMuted)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        WhatsProElectricBlue.copy(alpha = 0.65f),
                        WhatsProNeoRed.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // Atmospheric Electric Blue & Neo Red Canvas Glows
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WhatsProElectricBlue.copy(alpha = 0.24f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.15f),
                    radius = size.width * 0.45f
                ),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.9f, size.height * 0.15f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WhatsProNeoRed.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 1.0f),
                    radius = size.width * 0.35f
                ),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.15f, size.height * 1.0f)
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = WhatsProElectricBlue
                    ) {
                        Text(
                            text = workspace.planTier.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = WhatsProNeoRed.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WhatsProNeoRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(WhatsProNeoRed)
                            )
                            Text(
                                text = strings.sandboxChip.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                StatusPill(
                    label = if (workspace.aiEnabled) strings.dashAiReady else strings.dashAiPaused,
                    type = if (workspace.aiEnabled) "active" else "pending"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = workspace.companyName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${workspace.industry} • $openConversations ${strings.dashActiveThreadsSuffix} • $activeRulesCount ${strings.dashRulesSuffix}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))
            // Tri-color signature branding bar (Noir profond • Bleu électrique • Rouge néo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(WhatsProElectricBlue)
                )
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(WhatsProNeoRed)
                )
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }
        }
    }
}

@Composable
private fun SalesStatsGreenCard(
    totalSales: String,
    avgOrderValue: String,
    orderCount: Int,
    orders: List<Order>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val amounts = orders.take(6).reversed().map { it.totalAmount.toFloat() }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, WhatsProEmerald.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsProMintContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFECFDF5),
                            WhatsProMintContainer
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(WhatsProEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = strings.dashSalesStats,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.dashSalesStats,
                                style = MaterialTheme.typography.labelLarge,
                                color = WhatsProOnMint,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$orderCount ${strings.dashValidOrdersSuffix}",
                                style = MaterialTheme.typography.labelSmall,
                                color = WhatsProEmeraldDark
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = WhatsProEmerald
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${strings.dashAvgPrefix} $avgOrderValue",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = totalSales,
                            style = MaterialTheme.typography.headlineLarge,
                            color = WhatsProDeepBlack,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.dashSalesRevenueNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = WhatsProOnMint.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Green sparkline canvas
                    Canvas(
                        modifier = Modifier
                            .width(88.dp)
                            .height(38.dp)
                    ) {
                        val pts = if (amounts.size >= 2) amounts else listOf(18f, 34f, 26f, 48f, 42f, 64f)
                        val maxV = (pts.maxOrNull() ?: 1f).coerceAtLeast(1f)
                        val minV = pts.minOrNull() ?: 0f
                        val span = (maxV - minV).coerceAtLeast(1f)
                        val stepX = size.width / (pts.size - 1).coerceAtLeast(1)

                        val path = Path()
                        pts.forEachIndexed { i, v ->
                            val x = i * stepX
                            val norm = ((v - minV) / span).coerceIn(0.1f, 0.95f)
                            val y = size.height - norm * size.height
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = WhatsProEmeraldDark,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                        val lastNorm = ((pts.last() - minV) / span).coerceIn(0.1f, 0.95f)
                        drawCircle(
                            color = WhatsProEmeraldBright,
                            radius = 4.5.dp.toPx(),
                            center = Offset(size.width, size.height - lastNorm * size.height)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutomationPerformanceChartCard(
    totalHits: Long,
    ordersCount: Int,
    automations: List<AutomationRule>
) {
    val strings = LocalAppStrings.current
    val topRules = automations.take(5)
    val maxVal = (topRules.maxOfOrNull { it.hitCount } ?: 10L).coerceAtLeast(10L).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WhatsProBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.dashChartTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = WhatsProDeepBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.dashChartSubtitleFormat(totalHits, ordersCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = WhatsProElectricBlueContainer
                ) {
                    Text(
                        text = strings.dashTelemetryBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = WhatsProOnElectricBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (topRules.isEmpty()) {
                Text(
                    text = strings.dashChartEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val count = topRules.size
                    val spacing = 18.dp.toPx()
                    val barWidth = ((size.width - spacing * (count - 1)) / count).coerceAtLeast(16.dp.toPx())
                    val trackColor = WhatsProMintContainer
                    val activeBarColor = WhatsProEmerald
                    val mutedBarColor = Color(0xFF94A3B8)

                    topRules.forEachIndexed { idx, rule ->
                        val left = idx * (barWidth + spacing)
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(left, 0f),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                        val ratio = (rule.hitCount.toFloat() / maxVal).coerceIn(0.08f, 1f)
                        val barH = size.height * ratio
                        drawRoundRect(
                            color = if (rule.active) activeBarColor else mutedBarColor,
                            topLeft = Offset(left, size.height - barH),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topRules.forEach { rule ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${rule.hitCount}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhatsProDeepBlack
                            )
                            Text(
                                text = rule.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentInboxCard(
    conversations: List<Conversation>,
    onViewAll: () -> Unit,
    onOpenConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier.border(1.dp, WhatsProBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.dashRecentConversations,
                    style = MaterialTheme.typography.titleMedium,
                    color = WhatsProDeepBlack,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onViewAll,
                    modifier = Modifier.testTag("dashboard_view_inbox_btn")
                ) {
                    Text(strings.dashOpenInbox, color = WhatsProElectricBlue, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = WhatsProElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (conversations.isEmpty()) {
                Text(
                    text = strings.dashNoConversations,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    conversations.forEach { conv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenConversation(conv.id) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(WhatsProDeepBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conv.customerName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WhatsProElectricBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conv.customerName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WhatsProDeepBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = conv.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusPill(
                                label = localizedStatusLabel(conv.status),
                                type = conv.status
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentOrdersCard(
    orders: List<Order>,
    currency: String,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier.border(1.dp, WhatsProBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.dashRecentOrders,
                    style = MaterialTheme.typography.titleMedium,
                    color = WhatsProDeepBlack,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onViewAll,
                    modifier = Modifier.testTag("dashboard_view_orders_btn")
                ) {
                    Text(strings.dashAllOrders, color = WhatsProElectricBlue, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = WhatsProElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (orders.isEmpty()) {
                Text(
                    text = strings.dashNoOrders,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    orders.forEach { order ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${order.orderNumber} • ${order.customerName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = WhatsProDeepBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${order.items.sumOf { it.quantity }} ${strings.dashItemsSuffix} • ${formatCurrency(order.totalAmount, currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WhatsProEmeraldDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                StatusPill(
                                    label = localizedStatusLabel(order.status),
                                    type = order.status
                                )
                            }
                            PaymentMethodBadge(
                                methodId = order.paymentMethod,
                                paymentStatus = order.paymentStatus
                            )
                        }
                    }
                }
            }
        }
    }
}
