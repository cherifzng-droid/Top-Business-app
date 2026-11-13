package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.WorkspaceProfile
import com.example.ui.AiRunState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.components.formatShortTime
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedStatusLabel
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProEmeraldDark
import com.example.ui.theme.WhatsProMintContainer
import com.example.ui.theme.WhatsProOnMint
import com.example.ui.theme.WhatsProSkyContainer
import com.example.ui.theme.WhatsProTealAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboxScreen(
    workspace: WorkspaceProfile,
    conversations: List<Conversation>,
    selectedConversationId: String?,
    messages: List<ChatMessage>,
    aiSuggestionState: AiRunState,
    onSelectConversation: (String?) -> Unit,
    onSendReply: (String, String) -> Unit,
    onSimulateCustomer: (String, String) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onRequestAiDraft: (Conversation) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }

    val filteredConversations = remember(conversations, searchQuery, statusFilter) {
        conversations.filter { conv ->
            val matchesStatus = statusFilter == "all" || conv.status == statusFilter
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                conv.customerName.lowercase().contains(q) ||
                conv.lastMessage.lowercase().contains(q) ||
                conv.customerPhone.lowercase().contains(q)
            matchesStatus && matchesQuery
        }
    }

    val activeConversation = conversations.find { it.id == selectedConversationId }
        ?: filteredConversations.firstOrNull()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isSplitLayout = maxWidth >= 760.dp

        if (isSplitLayout) {
            Row(modifier = Modifier.fillMaxSize()) {
                ConversationListPane(
                    conversations = filteredConversations,
                    selectedId = activeConversation?.id,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { statusFilter = it },
                    onSelect = { onSelectConversation(it) },
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(WhatsProBorder)
                )
                if (activeConversation != null) {
                    ConversationDetailPane(
                        workspace = workspace,
                        conversation = activeConversation,
                        messages = messages,
                        aiSuggestionState = aiSuggestionState,
                        showBackButton = false,
                        onBack = {},
                        onSendReply = { text -> onSendReply(activeConversation.id, text) },
                        onSimulateCustomer = { text -> onSimulateCustomer(activeConversation.id, text) },
                        onUpdateStatus = { st -> onUpdateStatus(activeConversation.id, st) },
                        onRequestAiDraft = { onRequestAiDraft(activeConversation) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateCard(
                            title = strings.inboxSelectTitle,
                            subtitle = strings.inboxSelectSubtitle,
                            icon = Icons.Rounded.Forum
                        )
                    }
                }
            }
        } else {
            val showingDetail = selectedConversationId != null &&
                conversations.any { it.id == selectedConversationId }
            if (showingDetail && activeConversation != null) {
                BackHandler { onSelectConversation(null) }
                ConversationDetailPane(
                    workspace = workspace,
                    conversation = activeConversation,
                    messages = messages,
                    aiSuggestionState = aiSuggestionState,
                    showBackButton = true,
                    onBack = { onSelectConversation(null) },
                    onSendReply = { text -> onSendReply(activeConversation.id, text) },
                    onSimulateCustomer = { text -> onSimulateCustomer(activeConversation.id, text) },
                    onUpdateStatus = { st -> onUpdateStatus(activeConversation.id, st) },
                    onRequestAiDraft = { onRequestAiDraft(activeConversation) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ConversationListPane(
                    conversations = filteredConversations,
                    selectedId = selectedConversationId,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { statusFilter = it },
                    onSelect = { onSelectConversation(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConversationListPane(
    conversations: List<Conversation>,
    selectedId: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.sectionInbox, style = MaterialTheme.typography.headlineSmall)
            StatusPill(label = strings.sandboxChip, type = "pending")
        }
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(strings.inboxSearchPlaceholder) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inbox_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all", "open", "automated", "resolved").forEach { key ->
                FilterChip(
                    selected = statusFilter == key,
                    onClick = { onStatusFilterChange(key) },
                    label = { Text(localizedStatusLabel(key)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsProMintContainer,
                        selectedLabelColor = WhatsProOnMint
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        if (conversations.isEmpty()) {
            EmptyStateCard(
                title = strings.inboxNoMatchTitle,
                subtitle = strings.inboxNoMatchSubtitle,
                icon = Icons.Rounded.Forum
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(conversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == selectedId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                1.dp,
                                if (isSelected) WhatsProEmerald else WhatsProBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelect(conv.id) }
                            .testTag("conversation_row_${conv.id}"),
                        color = if (isSelected) WhatsProMintContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(WhatsProMintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conv.customerName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WhatsProEmeraldDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = conv.customerName,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (conv.unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(WhatsProEmerald)
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${conv.unreadCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = conv.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusPill(
                                        label = localizedStatusLabel(conv.status),
                                        type = conv.status
                                    )
                                    Text(
                                        text = formatShortTime(conv.updatedAt, strings.justNow),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationDetailPane(
    workspace: WorkspaceProfile,
    conversation: Conversation,
    messages: List<ChatMessage>,
    aiSuggestionState: AiRunState,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSendReply: (String) -> Unit,
    onSimulateCustomer: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onRequestAiDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var composerText by remember(conversation.id) { mutableStateOf("") }
    var simulateCustomerMode by remember(conversation.id) { mutableStateOf(false) }

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WhatsProMintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.customerName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = WhatsProEmeraldDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conversation.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${conversation.customerPhone} • ${strings.inboxSandboxHeaderSuffix}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val next = if (conversation.status == "resolved") "open" else "resolved"
                            onUpdateStatus(next)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (conversation.status == "resolved") strings.inboxReopen else strings.inboxResolve)
                    }
                }
                HorizontalDivider(color = WhatsProBorder)
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(message = msg)
            }
        }

        // AI Suggestion Card
        val suggestedText = when (aiSuggestionState) {
            is AiRunState.Success -> aiSuggestionState.text
            is AiRunState.Idle -> conversation.aiSuggestedReply
            else -> ""
        }

        if (aiSuggestionState is AiRunState.Loading || aiSuggestionState is AiRunState.Error || suggestedText.isNotBlank()) {
            Surface(
                color = WhatsProSkyContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = strings.inboxAiDraftTitle,
                                tint = WhatsProTealAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.inboxAiDraftTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = WhatsProTealAccent
                            )
                        }
                        if (suggestedText.isNotBlank()) {
                            AssistChip(
                                onClick = {
                                    simulateCustomerMode = false
                                    composerText = suggestedText
                                },
                                label = { Text(strings.inboxUseDraft) },
                                modifier = Modifier.testTag("use_ai_draft_chip")
                            )
                        }
                    }
                    when (aiSuggestionState) {
                        is AiRunState.Loading -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.inboxGeneratingAi, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is AiRunState.Error -> {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = aiSuggestionState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> if (suggestedText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = suggestedText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Composer
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                HorizontalDivider(color = WhatsProBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !simulateCustomerMode,
                        onClick = { simulateCustomerMode = false },
                        label = { Text(strings.inboxReplyAsAgent) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsProMintContainer,
                            selectedLabelColor = WhatsProOnMint
                        )
                    )
                    FilterChip(
                        selected = simulateCustomerMode,
                        onClick = { simulateCustomerMode = true },
                        label = { Text(strings.inboxSimulateCustomer) },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsProSkyContainer,
                            selectedLabelColor = WhatsProTealAccent
                        ),
                        modifier = Modifier.testTag("toggle_simulate_customer_chip")
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (workspace.aiEnabled) {
                        IconButton(
                            onClick = onRequestAiDraft,
                            modifier = Modifier.testTag("inbox_ai_suggest_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = strings.inboxAiDraftTitle,
                                tint = WhatsProTealAccent
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = composerText,
                        onValueChange = { composerText = it },
                        placeholder = {
                            Text(
                                if (simulateCustomerMode) strings.inboxSimulateHint
                                else "${strings.inboxReplyHintPrefix} ${conversation.customerName}..."
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inbox_message_input")
                    )
                    Button(
                        onClick = {
                            val txt = composerText.trim()
                            if (txt.isNotEmpty()) {
                                if (simulateCustomerMode) onSimulateCustomer(txt)
                                else onSendReply(txt)
                                composerText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (simulateCustomerMode) WhatsProTealAccent else WhatsProEmerald
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("inbox_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = strings.send)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val strings = LocalAppStrings.current
    val isCustomer = message.senderType == "customer"
    val isAutomation = message.senderType == "automation" || message.senderType == "ai"

    val bubbleColor = when {
        isCustomer -> MaterialTheme.colorScheme.surface
        isAutomation -> WhatsProMintContainer
        else -> WhatsProEmerald
    }
    val textColor = when {
        isCustomer -> MaterialTheme.colorScheme.onSurface
        isAutomation -> WhatsProOnMint
        else -> Color.White
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCustomer) Arrangement.Start else Arrangement.End
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCustomer) 4.dp else 16.dp,
                bottomEnd = if (isCustomer) 16.dp else 4.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier
                .widthIn(max = 420.dp)
                .then(
                    if (isCustomer) Modifier.border(1.dp, WhatsProBorder, RoundedCornerShape(16.dp))
                    else Modifier
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (isAutomation && message.ruleMatched.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = WhatsProEmeraldDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${strings.inboxAutoReplyPrefix} ${message.ruleMatched}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WhatsProEmeraldDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${localizedStatusLabel(message.senderType)} • ${formatShortTime(message.createdAt, strings.justNow)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.72f)
                )
            }
        }
    }
}
