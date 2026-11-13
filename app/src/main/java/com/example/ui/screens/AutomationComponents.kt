package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AutomationEngine
import com.example.model.AutomationMatchResult
import com.example.model.AutomationMatchStage
import com.example.model.AutomationRule
import com.example.model.WorkspaceProfile
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.WhatsProAmber
import com.example.ui.theme.WhatsProAmberContainer
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProDeepBlack
import com.example.ui.theme.WhatsProDeepBlackMuted
import com.example.ui.theme.WhatsProDeepBlackSurface
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProEmeraldBright
import com.example.ui.theme.WhatsProEmeraldDark
import com.example.ui.theme.WhatsProError
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProNeoRed
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProOnMint

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutomationHeroHeader(
    welcomeEnabled: Boolean,
    activeRuleCount: Int,
    totalHits: Long,
    onAddRule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsProDeepBlack)
    ) {
        Column {
            // Signature 3-color brand strip: Noir profond, Bleu électrique, Rouge néo + Emerald pulse
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                WhatsProElectricBlue,
                                WhatsProEmeraldBright,
                                WhatsProNeoRed
                            )
                        )
                    )
            )
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WhatsProDeepBlackMuted,
                            border = BorderStroke(1.dp, WhatsProElectricBlue.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = strings.autoVisualSandboxPill,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF93C5FD),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strings.autoTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.autoSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onAddRule,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsProEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("add_automation_button")
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = strings.autoAddRule,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.autoAddRule, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroStatChip(
                        icon = Icons.Rounded.WavingHand,
                        label = "${strings.autoStepWelcome}: ${if (welcomeEnabled) strings.autoActiveLabel else strings.autoOffLabel}",
                        accentColor = if (welcomeEnabled) WhatsProEmeraldBright else WhatsProNeoRed
                    )
                    HeroStatChip(
                        icon = Icons.Rounded.Bolt,
                        label = "$activeRuleCount ${strings.autoVisualStatsActiveLabel}",
                        accentColor = WhatsProElectricBlue
                    )
                    HeroStatChip(
                        icon = Icons.Rounded.DoneAll,
                        label = "$totalHits ${strings.autoVisualStatsHitsLabel}",
                        accentColor = WhatsProEmeraldBright
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatChip(
    icon: ImageVector,
    label: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = WhatsProDeepBlackSurface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisualRoutingAndSimulatorCard(
    workspace: WorkspaceProfile,
    welcomeEnabled: Boolean,
    keywordCount: Int,
    faqCount: Int,
    simulatorInput: String,
    onSimulatorInputChange: (String) -> Unit,
    matchResult: AutomationMatchResult,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val testPresets = if (lang == AppLanguage.FR) {
        listOf("Bonjour", "Prix", "Wave", "Horaires", "Livraison")
    } else {
        listOf("Hello", "Price", "Wave", "Hours", "Delivery")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, WhatsProBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(WhatsProElectricBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = WhatsProElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.autoPipelineTitle, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = strings.autoPipelineSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive 4-stage pipeline diagram
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PipelineStageNode(
                    title = strings.autoStepWelcome,
                    subtitle = if (welcomeEnabled) strings.autoActiveLabel else strings.autoOffLabel,
                    enabled = welcomeEnabled,
                    highlighted = matchResult.stage == AutomationMatchStage.WELCOME,
                    icon = Icons.Rounded.WavingHand
                )
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
                )
                PipelineStageNode(
                    title = strings.autoStepKeywords,
                    subtitle = "$keywordCount ${strings.autoActiveLabel.lowercase()}",
                    enabled = keywordCount > 0,
                    highlighted = matchResult.stage == AutomationMatchStage.KEYWORD,
                    icon = Icons.Rounded.Bolt
                )
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
                )
                PipelineStageNode(
                    title = strings.autoStepFaqs,
                    subtitle = "$faqCount ${strings.autoActiveLabel.lowercase()}",
                    enabled = faqCount > 0,
                    highlighted = matchResult.stage == AutomationMatchStage.FAQ,
                    icon = Icons.Rounded.QuestionAnswer
                )
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
                )
                PipelineStageNode(
                    title = strings.autoStepAi,
                    subtitle = if (workspace.aiEnabled) strings.autoReadyLabel else strings.autoOffLabel,
                    enabled = workspace.aiEnabled,
                    highlighted = matchResult.stage == AutomationMatchStage.AI_FALLBACK,
                    icon = Icons.Rounded.AutoAwesome,
                    isAi = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = WhatsProBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.autoTesterTitle,
                style = MaterialTheme.typography.labelLarge,
                color = WhatsProDeepBlack,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                testPresets.forEach { sample ->
                    val selected = simulatorInput.equals(sample, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = { onSimulatorInputChange(sample) },
                        label = { Text(sample) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsProDeepBlack,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("sim_chip_${sample.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = simulatorInput,
                onValueChange = onSimulatorInputChange,
                placeholder = { Text(strings.autoTesterHint) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("automation_tester_input")
            )

            AnimatedVisibility(
                visible = simulatorInput.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    SimulatorResultPreview(
                        matchResult = matchResult,
                        companyName = workspace.companyName
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineStageNode(
    title: String,
    subtitle: String,
    enabled: Boolean,
    highlighted: Boolean,
    icon: ImageVector,
    isAi: Boolean = false
) {
    val targetBg = when {
        highlighted && isAi -> WhatsProElectricBlue
        highlighted -> WhatsProEmerald
        !enabled -> MaterialTheme.colorScheme.surfaceVariant
        isAi -> WhatsProElectricBlueContainer
        else -> WhatsProMintContainer
    }
    val targetFg = when {
        highlighted -> Color.White
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        isAi -> WhatsProOnElectricBlue
        else -> WhatsProOnMint
    }
    val bg by animateColorAsState(targetValue = targetBg, label = "nodeBg")
    val fg by animateColorAsState(targetValue = targetFg, label = "nodeFg")

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = if (highlighted) {
            BorderStroke(1.5.dp, WhatsProDeepBlack)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(15.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = fg)
            }
        }
    }
}

@Composable
private fun SimulatorResultPreview(
    matchResult: AutomationMatchResult,
    companyName: String
) {
    val strings = LocalAppStrings.current
    val stageBadgeText = when (matchResult.stage) {
        AutomationMatchStage.WELCOME -> "${strings.autoTesterMatchedStagePrefix} ${strings.autoStepWelcome}"
        AutomationMatchStage.KEYWORD -> {
            val ruleName = matchResult.matchedRule?.name ?: strings.autoStepKeywords
            val kw = matchResult.matchedKeyword?.let { " • \"$it\"" }.orEmpty()
            "${strings.autoTesterMatchedStagePrefix} $ruleName$kw"
        }
        AutomationMatchStage.FAQ -> {
            val ruleName = matchResult.matchedRule?.name ?: strings.autoStepFaqs
            val kw = matchResult.matchedKeyword?.let { " • \"$it\"" }.orEmpty()
            "${strings.autoTesterMatchedStagePrefix} $ruleName$kw"
        }
        AutomationMatchStage.AI_FALLBACK -> strings.autoTesterAiFallback
        AutomationMatchStage.NONE -> strings.autoTesterNoMatch
    }

    val containerColor = when (matchResult.stage) {
        AutomationMatchStage.WELCOME, AutomationMatchStage.KEYWORD, AutomationMatchStage.FAQ -> WhatsProMintContainer
        AutomationMatchStage.AI_FALLBACK -> WhatsProElectricBlueContainer
        AutomationMatchStage.NONE -> WhatsProAmberContainer
    }
    val textColor = when (matchResult.stage) {
        AutomationMatchStage.WELCOME, AutomationMatchStage.KEYWORD, AutomationMatchStage.FAQ -> WhatsProOnMint
        AutomationMatchStage.AI_FALLBACK -> WhatsProOnElectricBlue
        AutomationMatchStage.NONE -> WhatsProDeepBlack
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("automation_simulator_result")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stageBadgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!matchResult.replyText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                WhatsAppOutgoingBubble(
                    senderLabel = companyName.ifBlank { "WhatsPro Business" },
                    message = matchResult.replyText,
                    timestamp = strings.justNow
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeMessageBuilderCard(
    workspace: WorkspaceProfile,
    welcomeEnabled: Boolean,
    welcomeMsg: String,
    hasUnsavedChanges: Boolean,
    onToggleWelcome: (Boolean) -> Unit,
    onWelcomeMsgChange: (String) -> Unit,
    onInsertToken: (String) -> Unit,
    onApplyPreset: (String) -> Unit,
    onSaveWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val variableTokens = listOf(
        "{entreprise}" to if (lang == AppLanguage.FR) "+ Entreprise" else "+ Company",
        "{horaires}" to if (lang == AppLanguage.FR) "+ Horaires" else "+ Hours",
        "{devise}" to if (lang == AppLanguage.FR) "+ Devise" else "+ Currency"
    )

    val presets = if (lang == AppLanguage.FR) {
        listOf(
            "Boutique" to "Bonjour et bienvenue chez {entreprise} ! Découvrez nos articles en {devise}. Tapez « prix », « livraison » ou « wave » pour une réponse immédiate. Horaires : {horaires}.",
            "Restaurant" to "Bonjour ! Merci de contacter {entreprise}. Tapez « menu » pour voir nos plats du jour ou « livraison » pour commander. Paiement Wave & Orange Money accepté.",
            "Services" to "Bienvenue chez {entreprise} ! Comment pouvons-nous vous aider aujourd'hui ? Nous sommes disponibles {horaires}."
        )
    } else {
        listOf(
            "Shop" to "Hello and welcome to {entreprise}! Browse our catalog in {devise}. Reply 'price', 'delivery', or 'wave' for instant answers. Hours: {horaires}.",
            "Restaurant" to "Hi! Thanks for messaging {entreprise}. Reply 'menu' for today's specials or 'delivery' to order. Wave & Orange Money accepted.",
            "Services" to "Welcome to {entreprise}! How can we assist you today? Our team is available {horaires}."
        )
    }

    val previewResolved = AutomationEngine.resolveTemplateVariables(
        template = welcomeMsg.ifBlank {
            if (lang == AppLanguage.FR) {
                "Bienvenue chez ${workspace.companyName.ifBlank { "WhatsPro" }} !"
            } else {
                "Welcome to ${workspace.companyName.ifBlank { "WhatsPro" }}!"
            }
        },
        workspace = workspace
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, WhatsProBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (welcomeEnabled) WhatsProMintContainer else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.WavingHand,
                            contentDescription = null,
                            tint = if (welcomeEnabled) WhatsProEmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(strings.autoWelcomeTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = strings.autoWelcomeSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = welcomeEnabled,
                    onCheckedChange = onToggleWelcome,
                    colors = SwitchDefaults.colors(checkedTrackColor = WhatsProEmerald),
                    modifier = Modifier.testTag("welcome_enabled_switch")
                )
            }

            if (!welcomeEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WhatsProAmberContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.autoWelcomeDisabledNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhatsProDeepBlack,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Variable Chips
            Text(
                text = strings.autoWelcomeVarsLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                variableTokens.forEach { (token, label) ->
                    AssistChip(
                        onClick = { onInsertToken(token) },
                        label = { Text(label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = WhatsProElectricBlueContainer,
                            labelColor = WhatsProOnElectricBlue
                        ),
                        modifier = Modifier.testTag("insert_var_${token.trim('{', '}')}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Starter Preset Templates
            Text(
                text = strings.autoWelcomePresetsLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (title, body) ->
                    AssistChip(
                        onClick = { onApplyPreset(body) },
                        label = { Text(title) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = WhatsProMintContainer,
                            labelColor = WhatsProOnMint
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = welcomeMsg,
                onValueChange = onWelcomeMsgChange,
                label = { Text(strings.autoGreetingLabel) },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_message_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Live WhatsApp Business Chat Preview
            WelcomeWhatsAppPreviewCard(
                companyName = workspace.companyName.ifBlank { "WhatsPro Business" },
                customerGreeting = if (lang == AppLanguage.FR) "Bonjour !" else "Hello!",
                resolvedWelcomeReply = previewResolved,
                previewTitle = strings.autoWelcomePreviewTitle,
                timestamp = strings.justNow
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasUnsavedChanges) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = WhatsProAmberContainer
                    ) {
                        Text(
                            text = strings.autoWelcomeUnsavedBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = WhatsProDeepBlack,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = onSaveWelcome,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsProEmerald),
                    modifier = Modifier.testTag("save_welcome_button")
                ) {
                    Text(strings.autoSaveWelcome, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun WelcomeWhatsAppPreviewCard(
    companyName: String,
    customerGreeting: String,
    resolvedWelcomeReply: String,
    previewTitle: String,
    timestamp: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFECE5DD),
        border = BorderStroke(1.dp, WhatsProBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("welcome_whatsapp_preview")
    ) {
        Column {
            // WhatsApp Business top bar styled in Deep Black
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhatsProDeepBlack)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(WhatsProEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = previewTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = WhatsProEmeraldBright
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customer incoming bubble (left)
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = customerGreeting,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhatsProDeepBlack,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                // Business auto-welcome outgoing bubble (right)
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    WhatsAppOutgoingBubble(
                        senderLabel = companyName,
                        message = resolvedWelcomeReply,
                        timestamp = timestamp
                    )
                }
            }
        }
    }
}

@Composable
fun WhatsAppOutgoingBubble(
    senderLabel: String,
    message: String,
    timestamp: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(0.92f),
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomEnd = 14.dp, bottomStart = 14.dp),
        color = Color(0xFFD9FDD3),
        border = BorderStroke(1.dp, WhatsProEmerald.copy(alpha = 0.25f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = senderLabel,
                style = MaterialTheme.typography.labelSmall,
                color = WhatsProEmeraldDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = WhatsProDeepBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = null,
                    tint = WhatsProElectricBlue,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisualRuleFlowCard(
    rule: AutomationRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val matchLabel = if (rule.matchMode == "exact") strings.autoMatchExact else strings.autoMatchContains

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (rule.active) WhatsProEmerald.copy(alpha = 0.45f) else WhatsProBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("automation_rule_card_${rule.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Rule Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = rule.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WhatsProElectricBlueContainer
                        ) {
                            Text(
                                text = rule.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = WhatsProOnElectricBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${rule.hitCount} ${strings.autoTriggersLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = rule.active,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = WhatsProEmerald),
                    modifier = Modifier.testTag("rule_switch_${rule.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage 1: Visual IF TRIGGER block
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, WhatsProBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.autoIfTriggerLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = WhatsProDeepBlack,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WhatsProDeepBlack
                        ) {
                            Text(
                                text = matchLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rule.keywords.forEach { kw ->
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (rule.active) WhatsProElectricBlueContainer else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (rule.active) WhatsProElectricBlue.copy(alpha = 0.4f) else WhatsProBorder
                                )
                            ) {
                                Text(
                                    text = kw,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (rule.active) WhatsProOnElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Visual Flow Connector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (rule.active) WhatsProMintContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = if (rule.active) WhatsProEmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Stage 2: Visual THEN AUTO-REPLY bubble
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (rule.active) WhatsProMintContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (rule.active) WhatsProEmerald.copy(alpha = 0.35f) else WhatsProBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = strings.autoThenReplyLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (rule.active) WhatsProEmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rule.responseText,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhatsProDeepBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_rule_${rule.id}")
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = strings.edit)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_rule_${rule.id}")
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = WhatsProError)
                }
            }
        }
    }
}
