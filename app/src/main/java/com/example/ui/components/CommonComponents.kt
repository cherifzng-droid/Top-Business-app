package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.WestAfricaPaymentMethod
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalSetAppLanguage
import com.example.ui.theme.WhatsProAmber
import com.example.ui.theme.WhatsProAmberContainer
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProDeepBlack
import com.example.ui.theme.WhatsProDeepBlackSurface
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProNeoRed
import com.example.ui.theme.WhatsProNeoRedContainer
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint
import com.example.ui.theme.WhatsProOnNeoRed
import com.example.ui.theme.WhatsProSuccess
import com.example.ui.theme.WhatsProSuccessContainer
import com.google.firebase.Timestamp
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LanguageTogglePill(
    modifier: Modifier = Modifier
) {
    val currentLang = LocalAppLanguage.current
    val setLang = LocalSetAppLanguage.current

    Surface(
        modifier = modifier
            .border(1.dp, WhatsProBorder, RoundedCornerShape(999.dp))
            .testTag("language_toggle_pill"),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLanguage.entries.forEach { lang ->
                val selected = currentLang == lang
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) WhatsProDeepBlack else Color.Transparent,
                    onClick = { setLang(lang) }
                ) {
                    Text(
                        text = lang.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WhatsProBrandBadge(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onDarkSurface: Boolean = false
) {
    val strings = LocalAppStrings.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 36.dp else 42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(WhatsProDeepBlack)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(WhatsProElectricBlue, WhatsProNeoRed)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = "WhatsPro brand icon",
                tint = WhatsProElectricBlue,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp)
            )
            // Rouge néo micro-accent dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(WhatsProNeoRed)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Whats",
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = if (onDarkSurface) Color.White else WhatsProDeepBlack,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Pro",
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = WhatsProElectricBlue,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (!compact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (onDarkSurface) Color.White.copy(alpha = 0.7f) else WhatsProDeepBlack)
                    )
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(WhatsProElectricBlue)
                    )
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(WhatsProNeoRed)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = strings.suiteTagline,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (onDarkSurface) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WhatsAppFoundationBanner(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = WhatsProDeepBlackSurface,
        border = BorderStroke(1.dp, WhatsProElectricBlue.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(WhatsProNeoRed.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudOff,
                    contentDescription = strings.whatsappNotConnectedTitle,
                    tint = WhatsProNeoRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.whatsappNotConnectedTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = WhatsProNeoRed
                    ) {
                        Text(
                            text = strings.phase2Badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.whatsappFoundationDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = WhatsProEmerald,
    containerColor: Color = WhatsProMintContainer,
    badgeText: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardModifier = modifier
        .border(1.dp, accentColor.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Crisp green accent top strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.4f))
                        )
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = WhatsProDeepBlack,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = containerColor
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    type: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (type.lowercase()) {
        "vip", "delivered", "resolved", "active", "automated", "paid" -> WhatsProSuccessContainer to WhatsProSuccess
        "confirmed", "shipped", "regular", "ai" -> WhatsProElectricBlueContainer to WhatsProOnElectricBlue
        "preparing", "pending", "open", "lead" -> WhatsProAmberContainer to WhatsProAmber
        "cancelled", "error", "disconnected" -> WhatsProNeoRedContainer to WhatsProOnNeoRed
        "wholesale" -> WhatsProMintContainer to WhatsProOnMint
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PaymentMethodBadge(
    methodId: String,
    paymentStatus: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val method = WestAfricaPaymentMethod.fromId(methodId)
    val accent = Color(method.accentColorHex)
    val isPaid = paymentStatus == "paid"

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = accent.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (method == WestAfricaPaymentMethod.CASH_ON_DELIVERY) {
                        Icons.Rounded.LocalShipping
                    } else {
                        Icons.Rounded.AccountBalanceWallet
                    },
                    contentDescription = method.displayName,
                    tint = if (method == WestAfricaPaymentMethod.MTN_MOMO) Color(0xFFB45309) else accent,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = WhatsProDeepBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (isPaid) WhatsProSuccessContainer else WhatsProAmberContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = if (isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = if (isPaid) WhatsProSuccess else WhatsProAmber,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isPaid) strings.paymentStatusPaid else strings.paymentStatusPending,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPaid) WhatsProSuccess else WhatsProAmber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ProductThumbnail(
    imageKey: String,
    imageUri: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    localImagePath: String = ""
) {
    val localFile = if (localImagePath.isNotBlank()) File(localImagePath) else null
    when {
        localFile != null && localFile.exists() -> {
            AsyncImage(
                model = localFile,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(RoundedCornerShape(12.dp))
            )
        }
        imageUri.isNotBlank() -> {
            AsyncImage(
                model = imageUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(RoundedCornerShape(12.dp))
            )
        }
        else -> {
            val resId = when (imageKey) {
                "bakery" -> R.drawable.img_product_bakery
                "gift" -> R.drawable.img_product_gift_box
                else -> R.drawable.img_product_coffee
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Rounded.Info,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WhatsProElectricBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = WhatsProElectricBlue
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CurrencySelectorBar(
    selectedCurrency: String,
    onSelectCurrency: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (lang == AppLanguage.FR) "Devise :" else "Currency:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        listOf("XOF", "GHS", "NGN", "EUR", "USD").forEach { cur ->
            val selected = selectedCurrency.equals(cur, ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) WhatsProEmerald else MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onSelectCurrency(cur) },
                modifier = Modifier.testTag("currency_pill_$cur")
            ) {
                Text(
                    text = cur,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

fun formatCurrency(amount: Double, currency: String): String {
    val cur = currency.uppercase()
    if (cur == "XOF" || cur == "FCFA") {
        val nf = NumberFormat.getIntegerInstance(Locale.FRANCE).apply {
            isGroupingUsed = true
        }
        return "${nf.format(kotlin.math.round(amount).toLong())} XOF"
    }
    if (cur == "NGN") {
        val nf = NumberFormat.getIntegerInstance(Locale.FRANCE).apply {
            isGroupingUsed = true
        }
        return "₦${nf.format(kotlin.math.round(amount).toLong())}"
    }
    val symbol = when (cur) {
        "EUR" -> "€"
        "GBP" -> "£"
        "GHS" -> "GH₵"
        "MAD" -> "MAD "
        "AED" -> "AED "
        "CAD" -> "CA$"
        else -> "$"
    }
    return "$symbol${String.format(Locale.US, "%.2f", amount)}"
}

fun formatShortTime(timestamp: Timestamp?, justNowLabel: String = "À l'instant"): String {
    val date = timestamp?.toDate() ?: return justNowLabel
    val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.FRANCE)
    return fmt.format(date)
}
