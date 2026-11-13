package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AutomationRule
import com.example.model.Product
import com.example.model.WestAfricaPaymentMethod
import com.example.model.WorkspaceProfile
import com.example.ui.AiRunState
import com.example.ui.components.StatusPill
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalSetAppLanguage
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProEmeraldDark
import com.example.ui.theme.WhatsProError
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint
import com.example.ui.theme.WhatsProSkyContainer
import com.example.ui.theme.WhatsProTealAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAssistantScreen(
    workspace: WorkspaceProfile,
    products: List<Product>,
    automations: List<AutomationRule>,
    sandboxState: AiRunState,
    onSaveWorkspace: (WorkspaceProfile, String) -> Unit,
    onTestSandbox: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentLang = LocalAppLanguage.current
    var aiEnabled by remember(workspace.aiEnabled) { mutableStateOf(workspace.aiEnabled) }
    var aiTone by remember(workspace.aiTone) { mutableStateOf(workspace.aiTone) }
    var aiLanguage by remember(workspace.aiLanguage) { mutableStateOf(workspace.aiLanguage) }
    var aiInstructions by remember(workspace.aiInstructions) { mutableStateOf(workspace.aiInstructions) }
    var sandboxPrompt by remember(currentLang) {
        mutableStateOf(
            if (currentLang == AppLanguage.FR) {
                "Bonjour, acceptez-vous Wave ou Orange Money et quels sont vos délais de livraison ?"
            } else {
                "Hello, do you accept Wave or Orange Money and what are your delivery times?"
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.aiTitle, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = strings.aiSubtitleFormat(products.count { it.active }, automations.count { it.active }),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.aiEnableTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = strings.aiEnableDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = aiEnabled,
                        onCheckedChange = { aiEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = WhatsProEmerald)
                    )
                }

                HorizontalDivider(color = WhatsProBorder)

                Text(strings.aiToneLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "friendly" to strings.aiToneFriendly,
                        "professional" to strings.aiToneProfessional,
                        "concise" to strings.aiToneConcise
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = aiTone == key,
                            onClick = { aiTone = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsProMintContainer,
                                selectedLabelColor = WhatsProOnMint
                            )
                        )
                    }
                }

                Text(strings.aiLanguageLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Français", "English", "Español", "العربية", "Wolof / Français").forEach { lang ->
                        FilterChip(
                            selected = aiLanguage == lang,
                            onClick = { aiLanguage = lang },
                            label = { Text(lang) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsProSkyContainer,
                                selectedLabelColor = WhatsProTealAccent
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = aiInstructions,
                    onValueChange = { aiInstructions = it },
                    label = { Text(strings.aiInstructionsLabel) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("ai_instructions_input")
                )

                Button(
                    onClick = {
                        onSaveWorkspace(
                            workspace.copy(
                                aiEnabled = aiEnabled,
                                aiTone = aiTone,
                                aiLanguage = aiLanguage,
                                aiInstructions = aiInstructions
                            ),
                            if (currentLang == AppLanguage.FR) "Configuration IA enregistrée." else "AI assistant settings saved."
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                    modifier = Modifier.testTag("save_ai_settings_button")
                ) {
                    Text(strings.aiSaveConfigBtn)
                }
            }
        }

        // Live Gemini Sandbox Tester
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = WhatsProTealAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.aiSandboxTitle, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = strings.aiSandboxDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = sandboxPrompt,
                    onValueChange = { sandboxPrompt = it },
                    label = { Text(strings.aiSandboxInputLabel) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("ai_sandbox_input")
                )
                Button(
                    onClick = { onTestSandbox(sandboxPrompt) },
                    enabled = aiEnabled && sandboxPrompt.isNotBlank() && sandboxState !is AiRunState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsProTealAccent),
                    modifier = Modifier.testTag("ai_sandbox_run_button")
                ) {
                    if (sandboxState is AiRunState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.aiSandboxThinking)
                    } else {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.aiSandboxTestBtn)
                    }
                }

                when (sandboxState) {
                    is AiRunState.Streaming -> {
                        Surface(
                            color = WhatsProMintContainer.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sandboxState.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    is AiRunState.Success -> {
                        Surface(
                            color = WhatsProMintContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sandboxState.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = WhatsProOnMint,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    is AiRunState.Error -> {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sandboxState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanyBillingScreen(
    workspace: WorkspaceProfile,
    userEmail: String,
    customerCount: Int,
    automationHits: Long,
    isBusy: Boolean,
    onSaveWorkspace: (WorkspaceProfile, String) -> Unit,
    onLoadDemoData: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentLang = LocalAppLanguage.current
    val setLang = LocalSetAppLanguage.current

    var companyName by remember(workspace.companyName) { mutableStateOf(workspace.companyName) }
    var industry by remember(workspace.industry) { mutableStateOf(workspace.industry) }
    var currency by remember(workspace.currency) { mutableStateOf(workspace.currency) }
    var email by remember(workspace.email) { mutableStateOf(workspace.email) }
    var phone by remember(workspace.phone) { mutableStateOf(workspace.phone) }
    var address by remember(workspace.address) { mutableStateOf(workspace.address) }
    var businessHours by remember(workspace.businessHours) { mutableStateOf(workspace.businessHours) }
    var planTier by remember(workspace.planTier) { mutableStateOf(workspace.planTier) }
    var acceptedPayments by remember(workspace.acceptedPaymentMethods) {
        mutableStateOf(
            workspace.acceptedPaymentMethods.ifEmpty {
                listOf("wave", "orange_money", "mtn_momo", "moov_money", "cash_on_delivery")
            }
        )
    }
    var mobileMoneyNumber by remember(workspace.mobileMoneyNumber) { mutableStateOf(workspace.mobileMoneyNumber) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.companyTitle, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = strings.companySubtitleFormat(workspace.ownerId.take(12), userEmail),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // App Language Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Language, contentDescription = null, tint = WhatsProElectricBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.companyAppLanguageTitle, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = strings.companyAppLanguageDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = currentLang == lang,
                            onClick = { setLang(lang) },
                            label = { Text(lang.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsProElectricBlueContainer,
                                selectedLabelColor = WhatsProOnElectricBlue
                            ),
                            modifier = Modifier.testTag("settings_lang_chip_${lang.code}")
                        )
                    }
                }
            }
        }

        // West Africa Mobile Money Configuration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProEmerald.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = WhatsProEmeraldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.companyMobileMoneyTitle, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = strings.companyMobileMoneyDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WestAfricaPaymentMethod.entries.forEach { method ->
                        val enabled = acceptedPayments.contains(method.id)
                        val accent = Color(method.accentColorHex)
                        FilterChip(
                            selected = enabled,
                            onClick = {
                                acceptedPayments = if (enabled && acceptedPayments.size > 1) {
                                    acceptedPayments - method.id
                                } else if (!enabled) {
                                    acceptedPayments + method.id
                                } else {
                                    acceptedPayments
                                }
                            },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(method.displayName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        method.regionNote,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.16f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("company_payment_toggle_${method.id}")
                        )
                    }
                }
                OutlinedTextField(
                    value = mobileMoneyNumber,
                    onValueChange = { mobileMoneyNumber = it },
                    label = { Text(strings.companyMobileMoneyNumberLabel) },
                    placeholder = { Text("+221 77 123 45 67") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mobile_money_number_input")
                )
                Button(
                    onClick = {
                        onSaveWorkspace(
                            workspace.copy(
                                acceptedPaymentMethods = acceptedPayments,
                                mobileMoneyNumber = mobileMoneyNumber.trim(),
                                currency = currency
                            ),
                            if (currentLang == AppLanguage.FR) {
                                "Moyens de paiement Mobile Money enregistrés."
                            } else {
                                "West Africa Mobile Money settings saved."
                            }
                        )
                    },
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                    modifier = Modifier.testTag("save_mobile_money_button")
                ) {
                    Text(strings.save)
                }
            }
        }

        // WhatsApp Cloud API Foundation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.companyWaCardTitle, style = MaterialTheme.typography.titleMedium)
                    }
                    StatusPill(label = strings.companyWaCardStatus, type = "pending")
                }
                Text(
                    text = strings.companyWaCardDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Company Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.companyDetailsTitle, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text(strings.companyNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("company_name_input")
                )
                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text(strings.companyIndustryLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(strings.companyEmailLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(strings.companyPhoneLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.companyAddressLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = businessHours,
                    onValueChange = { businessHours = it },
                    label = { Text(strings.companyHoursLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(strings.companyCurrencyLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("XOF", "GHS", "NGN", "EUR", "USD", "MAD", "AED").forEach { cur ->
                        FilterChip(
                            selected = currency == cur,
                            onClick = { currency = cur },
                            label = { Text(if (cur == "XOF") "XOF (FCFA)" else cur) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsProMintContainer,
                                selectedLabelColor = WhatsProOnMint
                            )
                        )
                    }
                }
                Button(
                    onClick = {
                        onSaveWorkspace(
                            workspace.copy(
                                companyName = companyName,
                                industry = industry,
                                currency = currency,
                                email = email,
                                phone = phone,
                                address = address,
                                businessHours = businessHours,
                                planTier = planTier,
                                acceptedPaymentMethods = acceptedPayments,
                                mobileMoneyNumber = mobileMoneyNumber.trim()
                            ),
                            if (currentLang == AppLanguage.FR) "Profil entreprise et paiements enregistrés." else "Company profile saved."
                        )
                    },
                    enabled = !isBusy && companyName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                    modifier = Modifier.testTag("save_company_button")
                ) {
                    Text(strings.companySaveProfileBtn)
                }
            }
        }

        // Subscription Plan Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = WhatsProEmeraldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.companyPlanTitle, style = MaterialTheme.typography.titleMedium)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${strings.companyContactsUsedLabel} $customerCount / ${if (planTier == "starter") 500 else if (planTier == "growth") 2500 else 10000}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = { (customerCount / 50f).coerceIn(0.05f, 1f) },
                        color = WhatsProEmerald,
                        trackColor = WhatsProMintContainer,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${strings.companyAutomationRunsLabel} $automationHits / 5 000 ${strings.companyMonthlySuffix}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = { (automationHits / 500f).coerceIn(0.05f, 1f) },
                        color = WhatsProTealAccent,
                        trackColor = WhatsProSkyContainer,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("starter", "Starter", "12 000 XOF/mois"),
                        Triple("growth", "Growth", "29 000 XOF/mois"),
                        Triple("enterprise", "Enterprise", "89 000 XOF/mois")
                    ).forEach { (tier, title, price) ->
                        val selected = planTier == tier
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) WhatsProMintContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (selected) WhatsProEmerald else WhatsProBorder
                            ),
                            onClick = {
                                planTier = tier
                                onSaveWorkspace(
                                    workspace.copy(planTier = tier),
                                    if (currentLang == AppLanguage.FR) "Forfait changé vers $title." else "Plan switched to $title."
                                )
                            }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    if (selected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = WhatsProEmeraldDark, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(price, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Workspace Actions (Demo Data + Sign Out)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onLoadDemoData,
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.companySeedDemoBtn)
            }
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = WhatsProError, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.companySignOutBtn, color = WhatsProError)
            }
        }
    }
}
