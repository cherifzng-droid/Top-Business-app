package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.AutomationEngine
import com.example.model.AutomationRule
import com.example.model.WorkspaceProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProDeepBlack
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProEmeraldDark
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutomationScreen(
    workspace: WorkspaceProfile,
    automations: List<AutomationRule>,
    onSaveWorkspace: (WorkspaceProfile, String) -> Unit,
    onSaveRule: (AutomationRule) -> Unit,
    onToggleRule: (String, Boolean) -> Unit,
    onDeleteRule: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentLang = LocalAppLanguage.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Keyword Replies, 1 = FAQs
    var welcomeEnabled by remember(workspace.welcomeEnabled) { mutableStateOf(workspace.welcomeEnabled) }
    var welcomeMsg by remember(workspace.welcomeMessage) { mutableStateOf(workspace.welcomeMessage) }
    var simulatorInput by remember { mutableStateOf("") }
    var editingRule by remember { mutableStateOf<AutomationRule?>(null) }

    val activeFilterType = if (selectedTab == 0) "keyword" else "faq"
    val displayedRules = remember(automations, activeFilterType) {
        automations.filter { it.triggerType == activeFilterType }
    }
    val hasWelcomeUnsavedChanges = welcomeEnabled != workspace.welcomeEnabled ||
        welcomeMsg != workspace.welcomeMessage

    // Live simulation using current in-progress welcome draft + saved rules
    val draftWorkspace = remember(workspace, welcomeEnabled, welcomeMsg) {
        workspace.copy(welcomeEnabled = welcomeEnabled, welcomeMessage = welcomeMsg)
    }
    val matchResult = remember(simulatorInput, draftWorkspace, automations) {
        AutomationEngine.evaluate(
            incomingText = simulatorInput,
            workspace = draftWorkspace,
            rules = automations
        )
    }

    val quickTemplates = remember(currentLang, workspace.currency) {
        buildQuickTemplates(currentLang, workspace.currency.ifBlank { "XOF" })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Dark Branded Hero Header
        item {
            AutomationHeroHeader(
                welcomeEnabled = welcomeEnabled,
                activeRuleCount = automations.count { it.active },
                totalHits = automations.sumOf { it.hitCount },
                onAddRule = { editingRule = AutomationRule(triggerType = activeFilterType) }
            )
        }

        // 2. Visual Routing Pipeline + Live Interactive Simulator
        item {
            VisualRoutingAndSimulatorCard(
                workspace = draftWorkspace,
                welcomeEnabled = welcomeEnabled,
                keywordCount = automations.count { it.triggerType == "keyword" && it.active },
                faqCount = automations.count { it.triggerType == "faq" && it.active },
                simulatorInput = simulatorInput,
                onSimulatorInputChange = { simulatorInput = it },
                matchResult = matchResult
            )
        }

        // 3. Visual Welcome Message Builder & WhatsApp Preview
        item {
            WelcomeMessageBuilderCard(
                workspace = workspace,
                welcomeEnabled = welcomeEnabled,
                welcomeMsg = welcomeMsg,
                hasUnsavedChanges = hasWelcomeUnsavedChanges,
                onToggleWelcome = { welcomeEnabled = it },
                onWelcomeMsgChange = { welcomeMsg = it },
                onInsertToken = { token ->
                    val spacer = if (welcomeMsg.isEmpty() || welcomeMsg.endsWith(" ")) "" else " "
                    welcomeMsg = "$welcomeMsg$spacer$token"
                },
                onApplyPreset = { preset -> welcomeMsg = preset },
                onSaveWelcome = {
                    onSaveWorkspace(
                        workspace.copy(
                            welcomeEnabled = welcomeEnabled,
                            welcomeMessage = welcomeMsg
                        ),
                        if (currentLang == AppLanguage.FR) {
                            "Message de bienvenue enregistré."
                        } else {
                            "Welcome message saved."
                        }
                    )
                }
            )
        }

        // 4. Quick-Start Keyword Rule Templates
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.autoQuickTemplatesTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = WhatsProDeepBlack,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickTemplates.forEachIndexed { idx, template ->
                        AssistChip(
                            onClick = { editingRule = template },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(template.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = WhatsProDeepBlack,
                                leadingIconContentColor = WhatsProEmeraldDark
                            ),
                            border = BorderStroke(1.dp, WhatsProBorder),
                            modifier = Modifier.testTag("quick_template_chip_$idx")
                        )
                    }
                }
            }
        }

        // 5. Tabs for Keyword Replies vs FAQs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = WhatsProEmeraldDark,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text("${strings.autoTabKeywords} (${automations.count { it.triggerType == "keyword" }})")
                    },
                    modifier = Modifier.testTag("auto_tab_keywords")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text("${strings.autoTabFaqs} (${automations.count { it.triggerType == "faq" }})")
                    },
                    modifier = Modifier.testTag("auto_tab_faqs")
                )
            }
        }

        // 6. Visual Trigger -> Reply Cards
        if (displayedRules.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (selectedTab == 0) strings.autoEmptyKeywordTitle else strings.autoEmptyFaqTitle,
                    subtitle = strings.autoEmptySubtitle,
                    icon = if (selectedTab == 0) Icons.Rounded.Bolt else Icons.AutoMirrored.Rounded.HelpOutline
                )
            }
        } else {
            items(displayedRules, key = { it.id }) { rule ->
                VisualRuleFlowCard(
                    rule = rule,
                    onToggle = { active -> onToggleRule(rule.id, active) },
                    onEdit = { editingRule = rule },
                    onDelete = { onDeleteRule(rule.id) }
                )
            }
        }
    }

    editingRule?.let { target ->
        AutomationRuleDialog(
            initial = target,
            companyName = workspace.companyName,
            onDismiss = { editingRule = null },
            onConfirm = { updated ->
                onSaveRule(updated)
                editingRule = null
            }
        )
    }
}

private fun buildQuickTemplates(lang: AppLanguage, currency: String): List<AutomationRule> {
    return if (lang == AppLanguage.FR) {
        listOf(
            AutomationRule(
                name = "Catalogue & Prix ($currency)",
                triggerType = "keyword",
                keywords = listOf("prix", "tarif", "catalogue", "menu"),
                matchMode = "contains",
                category = "Ventes",
                responseText = "Voici nos articles populaires en $currency ! Répondez avec le nom d'un produit pour commander ou obtenir plus de détails."
            ),
            AutomationRule(
                name = "Paiement Wave & Orange Money",
                triggerType = "keyword",
                keywords = listOf("wave", "orange", "momo", "payer", "paiement"),
                matchMode = "contains",
                category = "Paiement",
                responseText = "Nous acceptons Wave, Orange Money, MTN MoMo, Moov Money et espèces à la livraison. Envoyez votre capture de transfert pour validation rapide !"
            ),
            AutomationRule(
                name = "Livraison Express",
                triggerType = "faq",
                keywords = listOf("livraison", "livrer", "expédition", "frais"),
                matchMode = "contains",
                category = "Logistique",
                responseText = "Livraison express le jour même pour toute commande passée avant 15h. Indiquez votre quartier pour confirmer le tarif."
            ),
            AutomationRule(
                name = "Horaires & Boutique",
                triggerType = "faq",
                keywords = listOf("horaire", "ouvert", "adresse", "heures"),
                matchMode = "contains",
                category = "Infos",
                responseText = "Nous sommes ouverts du lundi au samedi ({horaires}). Contactez {entreprise} au {telephone} si besoin !"
            )
        )
    } else {
        listOf(
            AutomationRule(
                name = "Catalog & Pricing ($currency)",
                triggerType = "keyword",
                keywords = listOf("price", "menu", "catalog", "cost"),
                matchMode = "contains",
                category = "Sales",
                responseText = "Here are our featured items priced in $currency! Reply with a product name to place an order."
            ),
            AutomationRule(
                name = "Wave & Mobile Money",
                triggerType = "keyword",
                keywords = listOf("wave", "orange", "momo", "pay", "payment"),
                matchMode = "contains",
                category = "Payments",
                responseText = "We accept Wave, Orange Money, MTN MoMo, Moov Money, and Cash on Delivery. Share your payment reference once sent!"
            ),
            AutomationRule(
                name = "Express Delivery FAQ",
                triggerType = "faq",
                keywords = listOf("delivery", "shipping", "courier", "deliver"),
                matchMode = "contains",
                category = "Logistics",
                responseText = "Same-day express delivery is available for orders placed before 15:00. Share your neighborhood for exact fees."
            ),
            AutomationRule(
                name = "Store Hours & Location",
                triggerType = "faq",
                keywords = listOf("hours", "open", "address", "location"),
                matchMode = "contains",
                category = "Store Info",
                responseText = "{entreprise} is open Monday to Saturday ({horaires}). Reach us on {telephone} anytime!"
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AutomationRuleDialog(
    initial: AutomationRule,
    companyName: String,
    onDismiss: () -> Unit,
    onConfirm: (AutomationRule) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial.name) }
    var triggerType by remember { mutableStateOf(initial.triggerType.ifBlank { "keyword" }) }
    var matchMode by remember { mutableStateOf(initial.matchMode.ifBlank { "contains" }) }
    val keywordList = remember {
        mutableStateListOf<String>().apply {
            addAll(initial.keywords.map { it.trim() }.filter { it.isNotEmpty() })
        }
    }
    var pendingKeyword by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(initial.category.ifBlank { "Général" }) }
    var responseText by remember { mutableStateOf(initial.responseText) }

    fun commitPendingKeywords() {
        val parts = pendingKeyword
            .split(",", ";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        parts.forEach { kw ->
            if (keywordList.size < 12 && keywordList.none { it.equals(kw, ignoreCase = true) }) {
                keywordList.add(kw)
            }
        }
        pendingKeyword = ""
    }

    // Also allow saving even if user typed into pendingKeyword without pressing Add
    val effectiveKeywords = (keywordList + pendingKeyword.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() })
        .distinctBy { it.lowercase() }
        .take(12)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial.id.isBlank()) strings.autoNewRuleTitle else strings.autoEditRuleTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.autoRuleNameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_name_input")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "keyword" to strings.autoTriggerKeywordLabel,
                        "faq" to strings.autoTriggerFaqLabel
                    ).forEach { (k, label) ->
                        FilterChip(
                            selected = triggerType == k,
                            onClick = { triggerType = k },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsProMintContainer,
                                selectedLabelColor = WhatsProOnMint
                            )
                        )
                    }
                    listOf(
                        "contains" to strings.autoMatchContains,
                        "exact" to strings.autoMatchExact
                    ).forEach { (m, label) ->
                        FilterChip(
                            selected = matchMode == m,
                            onClick = { matchMode = m },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WhatsProElectricBlueContainer,
                                selectedLabelColor = WhatsProOnElectricBlue
                            )
                        )
                    }
                }

                // Visual keyword pill input + chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = pendingKeyword,
                        onValueChange = { input ->
                            if (input.contains(",")) {
                                pendingKeyword = input
                                commitPendingKeywords()
                            } else {
                                pendingKeyword = input
                            }
                        },
                        label = { Text(strings.autoKeywordChipHint) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rule_keywords_input")
                    )
                    FilledTonalButton(
                        onClick = { commitPendingKeywords() },
                        enabled = pendingKeyword.isNotBlank(),
                        modifier = Modifier.testTag("add_keyword_chip_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.autoAddKeywordBtn, modifier = Modifier.size(18.dp))
                    }
                }

                if (keywordList.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        keywordList.forEach { kw ->
                            InputChip(
                                selected = true,
                                onClick = { keywordList.remove(kw) },
                                label = { Text(kw) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = strings.delete,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = WhatsProElectricBlueContainer,
                                    selectedLabelColor = WhatsProOnElectricBlue,
                                    selectedTrailingIconColor = WhatsProOnElectricBlue
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(strings.catalogCategoryLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    label = { Text(strings.autoReplyMessageLabel) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_response_input")
                )

                if (responseText.isNotBlank()) {
                    Text(
                        text = strings.autoPreviewBubbleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFD9FDD3),
                        border = BorderStroke(1.dp, WhatsProEmerald.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = companyName.ifBlank { "WhatsPro Business" },
                                style = MaterialTheme.typography.labelSmall,
                                color = WhatsProEmeraldDark,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = responseText,
                                style = MaterialTheme.typography.bodySmall,
                                color = WhatsProDeepBlack
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        initial.copy(
                            name = name.trim(),
                            triggerType = triggerType,
                            matchMode = matchMode,
                            keywords = effectiveKeywords,
                            category = category.trim().ifBlank { "Général" },
                            responseText = responseText.trim()
                        )
                    )
                },
                enabled = name.isNotBlank() && effectiveKeywords.isNotEmpty() && responseText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                modifier = Modifier.testTag("save_rule_button")
            ) {
                Text(strings.autoSaveRuleBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
