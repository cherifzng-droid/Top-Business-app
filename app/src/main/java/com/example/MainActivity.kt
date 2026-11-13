package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.AiAssistantService
import com.example.data.WhatsProRepository
import com.example.firebase.configureAppCheck
import com.example.ui.UiState
import com.example.ui.WhatsProSection
import com.example.ui.WhatsProViewModel
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.WhatsProBrandBadge
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.EnglishStrings
import com.example.ui.i18n.FrenchStrings
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalSetAppLanguage
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CompanyBillingScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.WorkspaceOnboardingScreen
import com.example.ui.screens.signOutUser
import com.example.ui.theme.WhatsProBorder
import com.example.ui.theme.WhatsProDeepBlack
import com.example.ui.theme.WhatsProDeepBlackSurface
import com.example.ui.theme.WhatsProElectricBlue
import com.example.ui.theme.WhatsProElectricBlueContainer
import com.example.ui.theme.WhatsProEmerald
import com.example.ui.theme.WhatsProNeoRed
import com.example.ui.theme.WhatsProOnElectricBlue
import com.example.ui.theme.WhatsProTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before Compose UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val prefs = remember { getSharedPreferences("whatspro_prefs", Context.MODE_PRIVATE) }
            var appLanguage by remember {
                mutableStateOf(AppLanguage.fromCode(prefs.getString("app_lang", "FR")))
            }
            val setAppLanguage: (AppLanguage) -> Unit = remember {
                { next ->
                    appLanguage = next
                    prefs.edit().putString("app_lang", next.code).apply()
                }
            }
            val strings = if (appLanguage == AppLanguage.FR) FrenchStrings else EnglishStrings

            CompositionLocalProvider(
                LocalAppLanguage provides appLanguage,
                LocalSetAppLanguage provides setAppLanguage,
                LocalAppStrings provides strings
            ) {
                WhatsProTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        WhatsProRootNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsProRootNavigation() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        key(user.uid) {
            AuthenticatedWorkspaceApp(
                currentUserId = user.uid,
                userEmail = user.email.orEmpty()
            )
        }
    }
}

@Composable
private fun AuthenticatedWorkspaceApp(
    currentUserId: String,
    userEmail: String
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: WhatsProViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                WhatsProViewModel(
                    repository = WhatsProRepository(db, Firebase.auth),
                    aiService = AiAssistantService(),
                    currentUserId = currentUserId,
                    userEmail = userEmail
                )
            }
        }
    )

    val workspaceState by viewModel.workspaceState.collectAsStateWithLifecycle()
    val isActionBusy by viewModel.isActionBusy.collectAsStateWithLifecycle()

    when (val state = workspaceState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = WhatsProEmerald)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(strings.loadingWorkspace, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = strings.workspaceError,
                    subtitle = state.message
                )
            }
        }
        is UiState.Success -> {
            val workspace = state.data
            if (workspace == null) {
                WorkspaceOnboardingScreen(
                    userEmail = userEmail,
                    isBusy = isActionBusy,
                    onCreateWorkspace = { name, industry, currency, demo ->
                        viewModel.createWorkspace(name, industry, currency, demo)
                    },
                    onSignOut = {
                        signOutUser(credentialManager, scope)
                    }
                )
            } else {
                WhatsProMainShell(
                    viewModel = viewModel,
                    workspace = workspace,
                    userEmail = userEmail,
                    onSignOut = { signOutUser(credentialManager, scope) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhatsProMainShell(
    viewModel: WhatsProViewModel,
    workspace: com.example.model.WorkspaceProfile,
    userEmail: String,
    onSignOut: () -> Unit
) {
    val strings = LocalAppStrings.current
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val automations by viewModel.automations.collectAsStateWithLifecycle()
    val selectedConversationId by viewModel.selectedConversationId.collectAsStateWithLifecycle()
    val messages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val aiSuggestionState by viewModel.aiSuggestionState.collectAsStateWithLifecycle()
    val aiSandboxState by viewModel.aiSandboxState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isActionBusy.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    if (selectedSection != WhatsProSection.DASHBOARD) {
        BackHandler { viewModel.selectSection(WhatsProSection.DASHBOARD) }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val showPermanentSidebar = maxWidth >= 900.dp

        if (showPermanentSidebar) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                WhatsProSidebarContent(
                    workspaceName = workspace.companyName,
                    planTier = workspace.planTier,
                    selectedSection = selectedSection,
                    unreadCount = conversations.sumOf { it.unreadCount }.toInt(),
                    onSelect = { viewModel.selectSection(it) },
                    modifier = Modifier
                        .width(264.dp)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(WhatsProBorder)
                )
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.weight(1f)
                ) { innerPadding ->
                    WhatsProSectionContent(
                        section = selectedSection,
                        workspace = workspace,
                        userEmail = userEmail,
                        customers = customers,
                        products = products,
                        orders = orders,
                        conversations = conversations,
                        automations = automations,
                        selectedConversationId = selectedConversationId,
                        messages = messages,
                        aiSuggestionState = aiSuggestionState,
                        aiSandboxState = aiSandboxState,
                        isBusy = isBusy,
                        viewModel = viewModel,
                        onSignOut = onSignOut,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface
                    ) {
                        WhatsProSidebarContent(
                            workspaceName = workspace.companyName,
                            planTier = workspace.planTier,
                            selectedSection = selectedSection,
                            unreadCount = conversations.sumOf { it.unreadCount }.toInt(),
                            onSelect = { sec ->
                                viewModel.selectSection(sec)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("open_drawer_button")
                                ) {
                                    Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                                }
                            },
                            title = {
                                Column {
                                    Text(
                                        text = strings.sectionLabel(selectedSection),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = workspace.companyName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                LanguageTogglePill(modifier = Modifier.padding(end = 6.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = WhatsProDeepBlack,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CloudOff,
                                            contentDescription = strings.whatsappNotConnectedTitle,
                                            tint = WhatsProNeoRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = strings.sandboxChip,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        val primaryTabs = listOf(
                            WhatsProSection.DASHBOARD,
                            WhatsProSection.INBOX,
                            WhatsProSection.CUSTOMERS,
                            WhatsProSection.CATALOG,
                            WhatsProSection.ORDERS
                        )
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            primaryTabs.forEach { sec ->
                                val secLabel = strings.sectionLabel(sec)
                                NavigationBarItem(
                                    selected = selectedSection == sec,
                                    onClick = { viewModel.selectSection(sec) },
                                    icon = { Icon(sectionIcon(sec), contentDescription = secLabel) },
                                    label = {
                                        Text(
                                            text = secLabel,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WhatsProElectricBlue,
                                        selectedTextColor = WhatsProElectricBlue,
                                        indicatorColor = WhatsProElectricBlueContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("bottom_nav_${sec.route}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    WhatsProSectionContent(
                        section = selectedSection,
                        workspace = workspace,
                        userEmail = userEmail,
                        customers = customers,
                        products = products,
                        orders = orders,
                        conversations = conversations,
                        automations = automations,
                        selectedConversationId = selectedConversationId,
                        messages = messages,
                        aiSuggestionState = aiSuggestionState,
                        aiSandboxState = aiSandboxState,
                        isBusy = isBusy,
                        viewModel = viewModel,
                        onSignOut = onSignOut,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatsProSidebarContent(
    workspaceName: String,
    planTier: String,
    selectedSection: WhatsProSection,
    unreadCount: Int,
    onSelect: (WhatsProSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WhatsProBrandBadge()
            LanguageTogglePill()
        }
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = WhatsProDeepBlackSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsProElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = workspaceName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(WhatsProNeoRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${planTier.replaceFirstChar { it.uppercase() }} • ${strings.cloudApiOffSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = WhatsProBorder)
        Spacer(modifier = Modifier.height(10.dp))

        WhatsProSection.entries.forEach { sec ->
            val selected = selectedSection == sec
            val label = strings.sectionLabel(sec)
            NavigationDrawerItem(
                selected = selected,
                onClick = { onSelect(sec) },
                icon = { Icon(sectionIcon(sec), contentDescription = label) },
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                        if (sec == WhatsProSection.INBOX && unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(WhatsProNeoRed)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$unreadCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = WhatsProElectricBlueContainer,
                    selectedIconColor = WhatsProElectricBlue,
                    selectedTextColor = WhatsProOnElectricBlue,
                    unselectedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("sidebar_${sec.route}")
            )
        }
    }
}

@Composable
private fun WhatsProSectionContent(
    section: WhatsProSection,
    workspace: com.example.model.WorkspaceProfile,
    userEmail: String,
    customers: List<com.example.model.Customer>,
    products: List<com.example.model.Product>,
    orders: List<com.example.model.Order>,
    conversations: List<com.example.model.Conversation>,
    automations: List<com.example.model.AutomationRule>,
    selectedConversationId: String?,
    messages: List<com.example.model.ChatMessage>,
    aiSuggestionState: com.example.ui.AiRunState,
    aiSandboxState: com.example.ui.AiRunState,
    isBusy: Boolean,
    viewModel: WhatsProViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (section) {
            WhatsProSection.DASHBOARD -> DashboardScreen(
                workspace = workspace,
                customers = customers,
                conversations = conversations,
                orders = orders,
                automations = automations,
                onNavigate = { viewModel.selectSection(it) },
                onOpenConversation = { convId ->
                    viewModel.selectConversation(convId)
                    viewModel.selectSection(WhatsProSection.INBOX)
                }
            )
            WhatsProSection.INBOX -> InboxScreen(
                workspace = workspace,
                conversations = conversations,
                selectedConversationId = selectedConversationId,
                messages = messages,
                aiSuggestionState = aiSuggestionState,
                onSelectConversation = { viewModel.selectConversation(it) },
                onSendReply = { convId, text -> viewModel.sendAgentReply(convId, text) },
                onSimulateCustomer = { convId, text -> viewModel.simulateCustomerMessage(convId, text, workspace) },
                onUpdateStatus = { convId, status -> viewModel.updateConversationStatus(convId, status) },
                onRequestAiDraft = { conv -> viewModel.generateAiReplySuggestion(conv, workspace) }
            )
            WhatsProSection.CUSTOMERS -> CustomersScreen(
                currency = workspace.currency,
                customers = customers,
                onSaveCustomer = { viewModel.saveCustomer(it) },
                onDeleteCustomer = { viewModel.deleteCustomer(it) },
                onOpenChat = { viewModel.openConversationWithCustomer(it) }
            )
            WhatsProSection.CATALOG -> CatalogScreen(
                currency = workspace.currency,
                products = products,
                onSaveProduct = { viewModel.saveProduct(it) },
                onDeleteProduct = { viewModel.deleteProduct(it) },
                onSelectCurrency = { nextCur ->
                    viewModel.updateWorkspace(
                        workspace.copy(currency = nextCur),
                        "Devise d'affichage : $nextCur"
                    )
                }
            )
            WhatsProSection.ORDERS -> OrdersScreen(
                currency = workspace.currency,
                acceptedPaymentMethods = workspace.acceptedPaymentMethods,
                mobileMoneyNumber = workspace.mobileMoneyNumber,
                orders = orders,
                customers = customers,
                products = products,
                onCreateOrder = { cust, items, notes, paymentMethod, paymentStatus, paymentRef ->
                    viewModel.createOrder(
                        customer = cust,
                        items = items,
                        notes = notes,
                        paymentMethod = paymentMethod,
                        paymentStatus = paymentStatus,
                        paymentReference = paymentRef
                    )
                },
                onUpdateStatus = { orderId, st -> viewModel.updateOrderStatus(orderId, st) },
                onConfirmPayment = { orderId -> viewModel.updateOrderPaymentStatus(orderId, "paid") },
                onSelectCurrency = { nextCur ->
                    viewModel.updateWorkspace(
                        workspace.copy(currency = nextCur),
                        "Devise d'affichage : $nextCur"
                    )
                }
            )
            WhatsProSection.AUTOMATION -> AutomationScreen(
                workspace = workspace,
                automations = automations,
                onSaveWorkspace = { updated, msg -> viewModel.updateWorkspace(updated, msg) },
                onSaveRule = { viewModel.saveAutomationRule(it) },
                onToggleRule = { id, active -> viewModel.toggleAutomationRule(id, active) },
                onDeleteRule = { viewModel.deleteAutomationRule(it) }
            )
            WhatsProSection.AI_ASSISTANT -> AiAssistantScreen(
                workspace = workspace,
                products = products,
                automations = automations,
                sandboxState = aiSandboxState,
                onSaveWorkspace = { updated, msg -> viewModel.updateWorkspace(updated, msg) },
                onTestSandbox = { prompt -> viewModel.testAiSandbox(workspace, prompt) }
            )
            WhatsProSection.COMPANY_BILLING -> CompanyBillingScreen(
                workspace = workspace,
                userEmail = userEmail,
                customerCount = customers.size,
                automationHits = automations.sumOf { it.hitCount },
                isBusy = isBusy,
                onSaveWorkspace = { updated, msg -> viewModel.updateWorkspace(updated, msg) },
                onLoadDemoData = { viewModel.loadDemoDataIntoExistingWorkspace(workspace) },
                onSignOut = onSignOut
            )
        }
    }
}

private fun sectionIcon(section: WhatsProSection): ImageVector = when (section) {
    WhatsProSection.DASHBOARD -> Icons.Rounded.Dashboard
    WhatsProSection.INBOX -> Icons.Rounded.Forum
    WhatsProSection.CUSTOMERS -> Icons.Rounded.Groups
    WhatsProSection.CATALOG -> Icons.Rounded.Inventory2
    WhatsProSection.ORDERS -> Icons.Rounded.ShoppingBag
    WhatsProSection.AUTOMATION -> Icons.Rounded.AutoMode
    WhatsProSection.AI_ASSISTANT -> Icons.Rounded.AutoAwesome
    WhatsProSection.COMPANY_BILLING -> Icons.Rounded.Business
}
