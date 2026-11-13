package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiAssistantService
import com.example.data.WhatsProRepository
import com.example.model.AutomationRule
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.Customer
import com.example.model.Order
import com.example.model.OrderLineItem
import com.example.model.Product
import com.example.model.WorkspaceProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiRunState {
    data object Idle : AiRunState
    data object Loading : AiRunState
    data class Streaming(val text: String) : AiRunState
    data class Success(val text: String) : AiRunState
    data class Error(val message: String) : AiRunState
}

enum class WhatsProSection(val route: String, val label: String) {
    DASHBOARD("dashboard", "Dashboard"),
    INBOX("inbox", "Inbox"),
    CUSTOMERS("customers", "Customers"),
    CATALOG("catalog", "Catalog"),
    ORDERS("orders", "Orders"),
    AUTOMATION("automation", "Automation"),
    AI_ASSISTANT("ai_assistant", "AI Assistant"),
    COMPANY_BILLING("company_billing", "Company & Plan")
}

@OptIn(ExperimentalCoroutinesApi::class)
class WhatsProViewModel(
    private val repository: WhatsProRepository,
    private val aiService: AiAssistantService,
    private val currentUserId: String,
    private val userEmail: String
) : ViewModel() {

    private val _selectedSection = MutableStateFlow(WhatsProSection.DASHBOARD)
    val selectedSection: StateFlow<WhatsProSection> = _selectedSection.asStateFlow()

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isActionBusy = MutableStateFlow(false)
    val isActionBusy: StateFlow<Boolean> = _isActionBusy.asStateFlow()

    private val _aiSuggestionState = MutableStateFlow<AiRunState>(AiRunState.Idle)
    val aiSuggestionState: StateFlow<AiRunState> = _aiSuggestionState.asStateFlow()

    private val _aiSandboxState = MutableStateFlow<AiRunState>(AiRunState.Idle)
    val aiSandboxState: StateFlow<AiRunState> = _aiSandboxState.asStateFlow()

    val workspaceState: StateFlow<UiState<WorkspaceProfile?>> =
        repository.observeWorkspace(currentUserId)
            .map<WorkspaceProfile?, UiState<WorkspaceProfile?>> { UiState.Success(it) }
            .catch { error ->
                val root = unwrapFirestoreOrSelf(error)
                Log.w(TAG, "Error observing workspace", root)
                emit(UiState.Error(root.message ?: "Failed to load workspace"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val customers: StateFlow<List<Customer>> =
        repository.observeCustomers(currentUserId)
            .catch { error ->
                val root = unwrapFirestoreOrSelf(error)
                Log.w(TAG, "Error observing customers", root)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val products: StateFlow<List<Product>> =
        repository.observeProducts(currentUserId)
            .catch { error ->
                val root = unwrapFirestoreOrSelf(error)
                Log.w(TAG, "Error observing products", root)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val orders: StateFlow<List<Order>> =
        repository.observeOrders(currentUserId)
            .catch { error ->
                val root = unwrapFirestoreOrSelf(error)
                Log.w(TAG, "Error observing orders", root)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val conversations: StateFlow<List<Conversation>> =
        repository.observeConversations(currentUserId)
            .catch { error ->
                val root = unwrapFirestoreOrSelf(error)
                Log.w(TAG, "Error observing conversations", root)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val automations: StateFlow<List<AutomationRule>> =
        repository.observeAutomations(currentUserId)
            .catch { error ->
                val root = unwrapFirestoreOrSelf(error)
                Log.w(TAG, "Error observing automations", root)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val activeMessages: StateFlow<List<ChatMessage>> =
        _selectedConversationId
            .flatMapLatest { convId ->
                if (convId.isNullOrBlank()) flowOf(emptyList())
                else repository.observeMessages(currentUserId, convId)
            }
            .catch { error ->
                val root = unwrapFirestoreOrSelf(error)
                Log.w(TAG, "Error observing messages", root)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private fun unwrapFirestoreOrSelf(error: Throwable): Throwable {
        val fsError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (fsError == null && error is CancellationException) throw error
        return fsError ?: error
    }

    fun selectSection(section: WhatsProSection) {
        _selectedSection.value = section
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun selectConversation(conversationId: String?) {
        _selectedConversationId.value = conversationId
        _aiSuggestionState.value = AiRunState.Idle
        if (!conversationId.isNullOrBlank()) {
            viewModelScope.launch {
                try {
                    repository.markConversationRead(conversationId)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun createWorkspace(
        companyName: String,
        industry: String,
        currency: String,
        includeDemoData: Boolean
    ) {
        viewModelScope.launch {
            _isActionBusy.value = true
            try {
                if (includeDemoData) {
                    repository.seedDemoWorkspaceData(
                        companyName = companyName,
                        industry = industry,
                        currency = currency,
                        ownerEmail = userEmail
                    )
                    _statusMessage.value = "Espace créé avec les données de démonstration."
                } else {
                    repository.createOrUpdateWorkspace(
                        WorkspaceProfile(
                            companyName = companyName,
                            industry = industry,
                            currency = currency,
                            email = userEmail,
                            welcomeMessage = "Bonjour ! Bienvenue chez $companyName. Comment pouvons-nous vous aider aujourd'hui ?",
                            aiInstructions = "Répondez avec courtoisie sur les produits, commandes et paiements Mobile Money (Wave, Orange Money, MTN MoMo, Moov Money)."
                        ),
                        isCreate = true
                    )
                    _statusMessage.value = "Espace de travail privé créé."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec de création de l'espace."
            } finally {
                _isActionBusy.value = false
            }
        }
    }

    fun updateWorkspace(profile: WorkspaceProfile, toast: String = "Paramètres enregistrés.") {
        viewModelScope.launch {
            _isActionBusy.value = true
            try {
                repository.createOrUpdateWorkspace(profile, isCreate = false)
                _statusMessage.value = toast
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec d'enregistrement."
            } finally {
                _isActionBusy.value = false
            }
        }
    }

    fun loadDemoDataIntoExistingWorkspace(workspace: WorkspaceProfile) {
        viewModelScope.launch {
            _isActionBusy.value = true
            try {
                repository.seedDemoWorkspaceData(
                    companyName = workspace.companyName,
                    industry = workspace.industry,
                    currency = workspace.currency,
                    ownerEmail = workspace.email.ifBlank { userEmail }
                )
                _statusMessage.value = "Données de démonstration chargées dans votre espace."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec du chargement des données démo."
            } finally {
                _isActionBusy.value = false
            }
        }
    }

    // --- Customers ---

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.saveCustomer(customer)
                _statusMessage.value = if (customer.id.isBlank()) "Client ajouté." else "Client mis à jour."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec de l'enregistrement du client."
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(customerId)
                _statusMessage.value = "Client supprimé."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec de suppression du client."
            }
        }
    }

    fun openConversationWithCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                val convId = repository.startOrGetConversationForCustomer(customer)
                _selectedConversationId.value = convId
                _selectedSection.value = WhatsProSection.INBOX
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Impossible d'ouvrir la discussion."
            }
        }
    }

    // --- Products ---

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.saveProduct(product)
                _statusMessage.value = if (product.id.isBlank()) "Produit ajouté au catalogue." else "Produit mis à jour."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec d'enregistrement du produit."
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(productId)
                _statusMessage.value = "Produit supprimé."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec de suppression du produit."
            }
        }
    }

    // --- Orders & West Africa Mobile Money ---

    fun createOrder(
        customer: Customer,
        items: List<OrderLineItem>,
        notes: String,
        paymentMethod: String = "wave",
        paymentStatus: String = "pending",
        paymentReference: String = ""
    ) {
        viewModelScope.launch {
            try {
                repository.createOrder(
                    customer = customer,
                    items = items,
                    notes = notes,
                    paymentMethod = paymentMethod,
                    paymentStatus = paymentStatus,
                    paymentReference = paymentReference
                )
                _statusMessage.value = "Commande créée pour ${customer.name}."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec de création de commande."
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, newStatus)
                _statusMessage.value = "Statut de commande mis à jour."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec de mise à jour du statut."
            }
        }
    }

    fun updateOrderPaymentStatus(orderId: String, paymentStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderPaymentStatus(orderId, paymentStatus)
                _statusMessage.value = if (paymentStatus == "paid") {
                    "Paiement Mobile Money confirmé."
                } else {
                    "Statut de paiement mis à jour."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Échec de mise à jour du paiement."
            }
        }
    }

    // --- Automations ---

    fun saveAutomationRule(rule: AutomationRule) {
        viewModelScope.launch {
            try {
                repository.saveAutomationRule(rule)
                _statusMessage.value = if (rule.id.isBlank()) "Automation rule created." else "Rule updated."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Failed to save automation rule."
            }
        }
    }

    fun toggleAutomationRule(ruleId: String, active: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleAutomationRule(ruleId, active)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Failed to toggle rule."
            }
        }
    }

    fun deleteAutomationRule(ruleId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAutomationRule(ruleId)
                _statusMessage.value = "Automation rule deleted."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Failed to delete rule."
            }
        }
    }

    // --- Inbox & AI Assistant ---

    fun sendAgentReply(conversationId: String, text: String) {
        viewModelScope.launch {
            try {
                repository.sendMessage(conversationId, "agent", text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Failed to send message."
            }
        }
    }

    fun simulateCustomerMessage(
        conversationId: String,
        incomingText: String,
        workspace: WorkspaceProfile
    ) {
        viewModelScope.launch {
            try {
                val matched = repository.simulateIncomingCustomerMessage(
                    conversationId = conversationId,
                    incomingText = incomingText,
                    activeRules = automations.value,
                    workspace = workspace
                )
                if (matched != null) {
                    _statusMessage.value = "Automation triggered: $matched"
                } else {
                    _statusMessage.value = "Customer sandbox message received (no keyword match)."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Failed to simulate incoming message."
            }
        }
    }

    fun updateConversationStatus(conversationId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateConversationStatus(conversationId, status)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Failed to update conversation status."
            }
        }
    }

    fun generateAiReplySuggestion(conversation: Conversation, workspace: WorkspaceProfile) {
        viewModelScope.launch {
            _aiSuggestionState.value = AiRunState.Loading
            try {
                val suggestion = aiService.generateReplySuggestion(
                    workspace = workspace,
                    products = products.value,
                    rules = automations.value,
                    customerName = conversation.customerName,
                    recentMessages = activeMessages.value
                )
                _aiSuggestionState.value = AiRunState.Success(suggestion)
                repository.saveAiSuggestion(conversation.id, suggestion)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiSuggestionState.value = AiRunState.Error(
                    e.localizedMessage ?: "Failed to generate AI reply."
                )
            }
        }
    }

    fun testAiSandbox(workspace: WorkspaceProfile, customerMessage: String) {
        if (customerMessage.isBlank()) return
        viewModelScope.launch {
            _aiSandboxState.value = AiRunState.Loading
            var finalText = ""
            try {
                aiService.streamSandboxResponse(
                    workspace = workspace,
                    products = products.value,
                    rules = automations.value,
                    customerQuery = customerMessage.trim()
                ).collect { partial ->
                    finalText = partial
                    _aiSandboxState.value = AiRunState.Streaming(partial)
                }
                _aiSandboxState.value = AiRunState.Success(finalText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiSandboxState.value = AiRunState.Error(
                    e.localizedMessage ?: "AI Assistant failed to respond."
                )
            }
        }
    }

    private companion object {
        const val TAG = "WhatsProVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}
