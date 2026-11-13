package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.AutomationEngine
import com.example.model.AutomationMatchStage
import com.example.model.AutomationRule
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.Customer
import com.example.model.Order
import com.example.model.OrderLineItem
import com.example.model.Product
import com.example.model.WorkspaceProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WhatsProRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun workspaceDoc(workspaceId: String) =
        db.collection("workspaces").document(workspaceId)

    // --- Real-time Observers ---

    fun observeWorkspace(workspaceId: String): Flow<WorkspaceProfile?> = flow {
        requireUserId()
        val docRef = workspaceDoc(workspaceId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            WorkspaceProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    logStreamError(error, OperationType.GET, docRef.path, workspaceId)
                    throw error
                }
        )
    }

    fun observeCustomers(workspaceId: String): Flow<List<Customer>> = flow {
        requireUserId()
        val colRef = workspaceDoc(workspaceId).collection("customers")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(
                        Customer::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, colRef.path, workspaceId)
                    throw error
                }
        )
    }

    fun observeProducts(workspaceId: String): Flow<List<Product>> = flow {
        requireUserId()
        val colRef = workspaceDoc(workspaceId).collection("products")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(
                        Product::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.name.lowercase() }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, colRef.path, workspaceId)
                    throw error
                }
        )
    }

    fun observeOrders(workspaceId: String): Flow<List<Order>> = flow {
        requireUserId()
        val colRef = workspaceDoc(workspaceId).collection("orders")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(
                        Order::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, colRef.path, workspaceId)
                    throw error
                }
        )
    }

    fun observeConversations(workspaceId: String): Flow<List<Conversation>> = flow {
        requireUserId()
        val colRef = workspaceDoc(workspaceId).collection("conversations")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(
                        Conversation::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, colRef.path, workspaceId)
                    throw error
                }
        )
    }

    fun observeMessages(workspaceId: String, conversationId: String): Flow<List<ChatMessage>> = flow {
        requireUserId()
        val colRef = workspaceDoc(workspaceId)
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
        emitAll(
            colRef.orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ChatMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, colRef.path, workspaceId)
                    throw error
                }
        )
    }

    fun observeAutomations(workspaceId: String): Flow<List<AutomationRule>> = flow {
        requireUserId()
        val colRef = workspaceDoc(workspaceId).collection("automations")
        emitAll(
            colRef.snapshots()
                .map { snap ->
                    snap.toObjects(
                        AutomationRule::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.hitCount }
                }
                .catch { error ->
                    logStreamError(error, OperationType.LIST, colRef.path, workspaceId)
                    throw error
                }
        )
    }

    private fun logStreamError(
        error: Throwable,
        op: OperationType,
        path: String,
        workspaceId: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception) ?: return
        if (auth.currentUser?.uid == workspaceId) {
            handleFirestoreError(exceptionToLog, op, path, auth)
        }
    }

    // --- Workspace Onboarding & Profile ---

    suspend fun getWorkspace(workspaceId: String): WorkspaceProfile? {
        requireUserId()
        val docRef = workspaceDoc(workspaceId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(
                    WorkspaceProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun createOrUpdateWorkspace(profile: WorkspaceProfile, isCreate: Boolean) {
        val uid = requireUserId()
        val docRef = workspaceDoc(uid)
        val payload = buildMap<String, Any> {
            if (isCreate) {
                put("ownerId", uid)
                put("createdAt", FieldValue.serverTimestamp())
            }
            put("companyName", profile.companyName.trim().take(100))
            put("industry", profile.industry.trim().take(60))
            put("currency", profile.currency)
            put("planTier", profile.planTier)
            put("whatsappStatus", "not_connected")
            put("welcomeEnabled", profile.welcomeEnabled)
            put("welcomeMessage", profile.welcomeMessage.trim().take(1000))
            put("aiEnabled", profile.aiEnabled)
            put("aiTone", profile.aiTone)
            put("aiLanguage", profile.aiLanguage.trim().take(40))
            put("aiInstructions", profile.aiInstructions.trim().take(1500))
            put("email", profile.email.trim().take(120))
            put("phone", profile.phone.trim().take(40))
            put("address", profile.address.trim().take(200))
            put("businessHours", profile.businessHours.trim().take(120))
            put("acceptedPaymentMethods", profile.acceptedPaymentMethods.take(10))
            put("mobileMoneyNumber", profile.mobileMoneyNumber.trim().take(40))
            put("seededDemoData", profile.seededDemoData)
            put("updatedAt", FieldValue.serverTimestamp())
        }
        try {
            if (isCreate) {
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isCreate) OperationType.CREATE else OperationType.UPDATE,
                docRef.path,
                auth
            )
            throw e
        }
    }

    // --- Customers CRUD ---

    suspend fun saveCustomer(customer: Customer): String {
        val uid = requireUserId()
        val isNew = customer.id.isBlank()
        val docRef = if (isNew) {
            workspaceDoc(uid).collection("customers").document()
        } else {
            workspaceDoc(uid).collection("customers").document(customer.id)
        }
        val cleanTags = customer.tags.map { it.trim() }.filter { it.isNotEmpty() }.take(10)
        val payload = buildMap<String, Any> {
            if (isNew) {
                put("ownerId", uid)
                put("createdAt", FieldValue.serverTimestamp())
            }
            put("name", customer.name.trim().take(100))
            put("phone", customer.phone.trim().take(40))
            put("email", customer.email.trim().take(120))
            put("company", customer.company.trim().take(100))
            put("segment", customer.segment)
            put("tags", cleanTags)
            put("notes", customer.notes.trim().take(1000))
            put("totalOrders", customer.totalOrders)
            put("totalSpent", customer.totalSpent)
            put("updatedAt", FieldValue.serverTimestamp())
        }
        return try {
            if (isNew) docRef.set(payload).await() else docRef.update(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isNew) OperationType.CREATE else OperationType.UPDATE,
                docRef.path,
                auth
            )
            throw e
        }
    }

    suspend fun deleteCustomer(customerId: String) {
        val uid = requireUserId()
        val docRef = workspaceDoc(uid).collection("customers").document(customerId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    // --- Products CRUD ---

    suspend fun saveProduct(product: Product): String {
        val uid = requireUserId()
        val isNew = product.id.isBlank()
        val docRef = if (isNew) {
            workspaceDoc(uid).collection("products").document()
        } else {
            workspaceDoc(uid).collection("products").document(product.id)
        }
        val payload = buildMap<String, Any> {
            if (isNew) {
                put("ownerId", uid)
                put("createdAt", FieldValue.serverTimestamp())
            }
            put("name", product.name.trim().take(120))
            put("description", product.description.trim().take(1000))
            put("category", product.category.trim().ifEmpty { "General" }.take(60))
            put("sku", product.sku.trim().take(50))
            put("price", product.price.coerceAtLeast(0.0))
            put("stock", product.stock.coerceAtLeast(0L))
            put("active", product.active)
            put("imageKey", product.imageKey.ifBlank { "coffee" }.take(60))
            if (product.imageUri.isNotBlank()) {
                put("imageUri", product.imageUri.take(600))
            }
            if (product.localImagePath.isNotBlank()) {
                put("localImagePath", product.localImagePath.take(600))
            }
            put("updatedAt", FieldValue.serverTimestamp())
        }
        return try {
            if (isNew) docRef.set(payload).await() else docRef.update(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isNew) OperationType.CREATE else OperationType.UPDATE,
                docRef.path,
                auth
            )
            throw e
        }
    }

    suspend fun deleteProduct(productId: String) {
        val uid = requireUserId()
        val docRef = workspaceDoc(uid).collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    // --- Orders CRUD ---

    suspend fun createOrder(
        customer: Customer,
        items: List<OrderLineItem>,
        notes: String,
        status: String = "pending",
        paymentMethod: String = "wave",
        paymentStatus: String = "pending",
        paymentReference: String = ""
    ): String {
        val uid = requireUserId()
        require(items.isNotEmpty()) { "Order must contain at least one product." }
        val boundedItems = items.take(20)
        val total = boundedItems.sumOf { it.quantity * it.unitPrice }
        val orderNum = "WP-${(1000..9999).random()}"
        val docRef = workspaceDoc(uid).collection("orders").document()
        val payload = mapOf(
            "ownerId" to uid,
            "orderNumber" to orderNum,
            "customerId" to customer.id,
            "customerName" to customer.name.trim().take(100),
            "customerPhone" to customer.phone.trim().take(40),
            "items" to boundedItems.map { it.toMap() },
            "totalAmount" to total,
            "status" to status,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to paymentStatus,
            "paymentReference" to paymentReference.trim().take(80),
            "notes" to notes.trim().take(500),
            "channel" to "sandbox",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            // Update customer aggregate stats if customer document exists
            if (customer.id.isNotBlank()) {
                workspaceDoc(uid).collection("customers").document(customer.id)
                    .update(
                        mapOf(
                            "totalOrders" to FieldValue.increment(1),
                            "totalSpent" to FieldValue.increment(total),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        val uid = requireUserId()
        val docRef = workspaceDoc(uid).collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun updateOrderPaymentStatus(orderId: String, paymentStatus: String) {
        val uid = requireUserId()
        val docRef = workspaceDoc(uid).collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "paymentStatus" to paymentStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    // --- Automations & FAQ CRUD ---

    suspend fun saveAutomationRule(rule: AutomationRule): String {
        val uid = requireUserId()
        val isNew = rule.id.isBlank()
        val docRef = if (isNew) {
            workspaceDoc(uid).collection("automations").document()
        } else {
            workspaceDoc(uid).collection("automations").document(rule.id)
        }
        val cleanKeywords = rule.keywords
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .take(10)
            .ifEmpty { listOf(rule.name.trim().lowercase().ifEmpty { "help" }) }

        val payload = buildMap<String, Any> {
            if (isNew) {
                put("ownerId", uid)
                put("createdAt", FieldValue.serverTimestamp())
            }
            put("name", rule.name.trim().take(100))
            put("triggerType", rule.triggerType)
            put("keywords", cleanKeywords)
            put("matchMode", rule.matchMode)
            put("responseText", rule.responseText.trim().take(2000))
            put("category", rule.category.trim().ifEmpty { "General" }.take(60))
            put("active", rule.active)
            put("hitCount", rule.hitCount)
            put("updatedAt", FieldValue.serverTimestamp())
        }
        return try {
            if (isNew) docRef.set(payload).await() else docRef.update(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isNew) OperationType.CREATE else OperationType.UPDATE,
                docRef.path,
                auth
            )
            throw e
        }
    }

    suspend fun toggleAutomationRule(ruleId: String, active: Boolean) {
        val uid = requireUserId()
        val docRef = workspaceDoc(uid).collection("automations").document(ruleId)
        try {
            docRef.update(
                mapOf(
                    "active" to active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun deleteAutomationRule(ruleId: String) {
        val uid = requireUserId()
        val docRef = workspaceDoc(uid).collection("automations").document(ruleId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    // --- Conversations & Sandbox Messaging ---

    suspend fun startOrGetConversationForCustomer(customer: Customer): String {
        val uid = requireUserId()
        val colRef = workspaceDoc(uid).collection("conversations")
        return try {
            val existing = colRef.whereEqualTo("customerId", customer.id).limit(1).get().await()
            if (!existing.isEmpty) {
                existing.documents.first().id
            } else {
                val newDoc = colRef.document()
                val welcomeMsg = "New sandbox conversation started with ${customer.name}."
                newDoc.set(
                    mapOf(
                        "ownerId" to uid,
                        "customerId" to customer.id,
                        "customerName" to customer.name.take(100),
                        "customerPhone" to customer.phone.take(40),
                        "status" to "open",
                        "unreadCount" to 0L,
                        "lastMessage" to welcomeMsg,
                        "lastMessageSender" to "agent",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                newDoc.id
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, colRef.path, auth)
            throw e
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderType: String,
        text: String,
        ruleMatched: String = ""
    ) {
        val uid = requireUserId()
        val convRef = workspaceDoc(uid).collection("conversations").document(conversationId)
        val msgRef = convRef.collection("messages").document()
        val cleanText = text.trim().take(4000)
        if (cleanText.isEmpty()) return

        val msgPayload = buildMap<String, Any> {
            put("ownerId", uid)
            put("senderType", senderType)
            put("text", cleanText)
            if (ruleMatched.isNotBlank()) {
                put("ruleMatched", ruleMatched.take(100))
            }
            put("createdAt", FieldValue.serverTimestamp())
        }

        val convUpdate = buildMap<String, Any> {
            put("lastMessage", cleanText.take(2000))
            put("lastMessageSender", senderType)
            put("unreadCount", if (senderType == "customer") FieldValue.increment(1) else 0L)
            put("status", if (senderType == "automation") "automated" else "open")
            if (ruleMatched.isNotBlank()) {
                put("assignedRule", ruleMatched.take(100))
            }
            put("updatedAt", FieldValue.serverTimestamp())
        }

        try {
            db.runBatch { batch ->
                batch.set(msgRef, msgPayload)
                batch.update(convRef, convUpdate)
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, msgRef.path, auth)
            throw e
        }
    }

    suspend fun simulateIncomingCustomerMessage(
        conversationId: String,
        incomingText: String,
        activeRules: List<AutomationRule>,
        workspace: WorkspaceProfile
    ): String? {
        val uid = requireUserId()
        sendMessage(conversationId, "customer", incomingText)

        val evaluation = AutomationEngine.evaluate(
            incomingText = incomingText,
            workspace = workspace,
            rules = activeRules
        )

        val matchedRule = evaluation.matchedRule
        val reply = evaluation.replyText
        if (matchedRule != null && !reply.isNullOrBlank()) {
            sendMessage(
                conversationId = conversationId,
                senderType = "automation",
                text = reply,
                ruleMatched = matchedRule.name
            )
            workspaceDoc(uid).collection("automations").document(matchedRule.id)
                .update(
                    mapOf(
                        "hitCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            return matchedRule.name
        }

        if (evaluation.stage == AutomationMatchStage.WELCOME && !reply.isNullOrBlank()) {
            sendMessage(
                conversationId = conversationId,
                senderType = "automation",
                text = reply,
                ruleMatched = "Welcome Message"
            )
            return "Welcome Message"
        }

        return null
    }

    suspend fun markConversationRead(conversationId: String) {
        val uid = requireUserId()
        val convRef = workspaceDoc(uid).collection("conversations").document(conversationId)
        try {
            convRef.update(
                mapOf(
                    "unreadCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path, auth)
        }
    }

    suspend fun updateConversationStatus(conversationId: String, status: String) {
        val uid = requireUserId()
        val convRef = workspaceDoc(uid).collection("conversations").document(conversationId)
        try {
            convRef.update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path, auth)
            throw e
        }
    }

    suspend fun saveAiSuggestion(conversationId: String, suggestion: String) {
        val uid = requireUserId()
        val convRef = workspaceDoc(uid).collection("conversations").document(conversationId)
        try {
            convRef.update(
                mapOf(
                    "aiSuggestedReply" to suggestion.take(2000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path, auth)
        }
    }

    // --- Initial Sample Data Seeder (strictly scoped to user's private workspace) ---

    suspend fun seedDemoWorkspaceData(companyName: String, industry: String, currency: String, ownerEmail: String) {
        val uid = requireUserId()
        val wsRef = workspaceDoc(uid)
        val alreadyExists = wsRef.get().await().exists()

        val wsPayload = buildMap<String, Any> {
            if (!alreadyExists) {
                put("ownerId", uid)
                put("createdAt", FieldValue.serverTimestamp())
            }
            put("companyName", companyName.trim().ifEmpty { "Atlas Roastery & Bakery" }.take(100))
            put("industry", industry.trim().ifEmpty { "Restaurant & Cafe" }.take(60))
            put("currency", currency.ifBlank { "USD" })
            put("email", ownerEmail.ifBlank { "hello@whatspro-demo.com" }.take(120))
            put("phone", "+1 (415) 555-0192")
            put("address", "420 Market Street, Suite 3B")
            put("businessHours", "Lun-Sam 08:00 - 20:00")
            put("planTier", "growth")
            put("whatsappStatus", "not_connected")
            put("welcomeEnabled", true)
            put("welcomeMessage", "Bonjour ! Bienvenue chez ${companyName.ifBlank { "notre boutique" }}. Répondez MENU pour nos produits, HORAIRES pour nos heures d'ouverture, ou posez votre question !")
            put("aiEnabled", true)
            put("aiTone", "friendly")
            put("aiLanguage", "Français")
            put("aiInstructions", "Mentionnez nos options de livraison et les paiements Mobile Money (Wave, Orange Money, MTN MoMo, Moov Money). Réponses courtes et chaleureuses.")
            put("acceptedPaymentMethods", listOf("wave", "orange_money", "mtn_momo", "moov_money", "mixx_by_yas", "cash_on_delivery"))
            put("mobileMoneyNumber", "+221 77 555 01 92")
            put("seededDemoData", true)
            put("updatedAt", FieldValue.serverTimestamp())
        }

        val custIds = List(5) { "demo_cust_${UUID.randomUUID().toString().take(8)}" }
        val prodIds = List(4) { "demo_prod_${UUID.randomUUID().toString().take(8)}" }
        val orderIds = List(4) { "demo_ord_${UUID.randomUUID().toString().take(8)}" }
        val convIds = List(4) { "demo_conv_${UUID.randomUUID().toString().take(8)}" }
        val ruleIds = List(5) { "demo_rule_${UUID.randomUUID().toString().take(8)}" }

        try {
            db.runBatch { batch ->
                if (alreadyExists) {
                    batch.update(wsRef, wsPayload)
                } else {
                    batch.set(wsRef, wsPayload)
                }

                val customers = listOf(
                    Triple(custIds[0], "Nadia Benali", listOf("+1 415 890 1122", "nadia@boutiquecafe.co", "Boutique Cafe", "vip", "Prefers whole-bean espresso & morning delivery")),
                    Triple(custIds[1], "Marcus Vance", listOf("+44 7700 900123", "marcus@vanceevents.uk", "Vance Events", "wholesale", "Monthly corporate pastry catering")),
                    Triple(custIds[2], "Elena Rostova", listOf("+33 6 12 34 56 78", "elena.r@studio.fr", "", "regular", "Orders weekend gift boxes")),
                    Triple(custIds[3], "Kenji Takahashi", listOf("+81 90 1234 5678", "kenji@designlab.jp", "DesignLab", "vip", "Regular pour-over roast subscriber")),
                    Triple(custIds[4], "Sofia Morales", listOf("+34 611 223 344", "sofia@atelier.es", "", "lead", "Asked about allergen-free pastry boxes"))
                )
                val customerTotals = listOf(Pair(6L, 284.5), Pair(12L, 1420.0), Pair(3L, 145.0), Pair(8L, 392.0), Pair(0L, 0.0))

                customers.forEachIndexed { idx, (cid, name, meta) ->
                    val totals = customerTotals[idx]
                    batch.set(
                        wsRef.collection("customers").document(cid),
                        mapOf(
                            "ownerId" to uid,
                            "name" to name,
                            "phone" to meta[0],
                            "email" to meta[1],
                            "company" to meta[2],
                            "segment" to meta[3],
                            "tags" to listOf(meta[3], "sandbox-demo"),
                            "notes" to meta[4],
                            "totalOrders" to totals.first,
                            "totalSpent" to totals.second,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val products = listOf(
                    listOf(prodIds[0], "Single-Origin Espresso Roast", "Balanced dark chocolate & hazelnut notes, 250g whole bean.", "Coffee", "ESP-250", 19.50, 48L, "coffee"),
                    listOf(prodIds[1], "Artisan Croissant Box (6 pcs)", "Slow-fermented French butter croissants baked fresh daily.", "Bakery", "BAK-006", 24.00, 18L, "bakery"),
                    listOf(prodIds[2], "Signature Gift Hamper", "Curated specialty tea, roasted beans & ceramic keepsake box.", "Gift Sets", "GFT-100", 54.00, 12L, "gift"),
                    listOf(prodIds[3], "Cold Brew Concentrate (1L)", "Steeped 18 hours for smooth iced lattes at home or office.", "Beverages", "BEV-010", 16.00, 32L, "coffee")
                )

                products.forEach { p ->
                    batch.set(
                        wsRef.collection("products").document(p[0] as String),
                        mapOf(
                            "ownerId" to uid,
                            "name" to p[1],
                            "description" to p[2],
                            "category" to p[3],
                            "sku" to p[4],
                            "price" to p[5],
                            "stock" to p[6],
                            "active" to true,
                            "imageKey" to p[7],
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val orders = listOf(
                    mapOf(
                        "ownerId" to uid,
                        "orderNumber" to "WP-2048",
                        "customerId" to custIds[0],
                        "customerName" to "Nadia Benali",
                        "customerPhone" to "+1 415 890 1122",
                        "items" to listOf(
                            OrderLineItem(prodIds[0], "Single-Origin Espresso Roast", 2, 19.50).toMap(),
                            OrderLineItem(prodIds[1], "Artisan Croissant Box (6 pcs)", 1, 24.00).toMap()
                        ),
                        "totalAmount" to 63.00,
                        "status" to "preparing",
                        "paymentMethod" to "wave",
                        "paymentStatus" to "paid",
                        "paymentReference" to "WV-88412",
                        "notes" to "Livrer avant 10h00",
                        "channel" to "sandbox",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "ownerId" to uid,
                        "orderNumber" to "WP-2047",
                        "customerId" to custIds[1],
                        "customerName" to "Marcus Vance",
                        "customerPhone" to "+44 7700 900123",
                        "items" to listOf(
                            OrderLineItem(prodIds[1], "Artisan Croissant Box (6 pcs)", 5, 24.00).toMap(),
                            OrderLineItem(prodIds[3], "Cold Brew Concentrate (1L)", 4, 16.00).toMap()
                        ),
                        "totalAmount" to 184.00,
                        "status" to "confirmed",
                        "paymentMethod" to "orange_money",
                        "paymentStatus" to "paid",
                        "paymentReference" to "OM-55209",
                        "notes" to "Commande événementiel",
                        "channel" to "sandbox",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "ownerId" to uid,
                        "orderNumber" to "WP-2046",
                        "customerId" to custIds[2],
                        "customerName" to "Elena Rostova",
                        "customerPhone" to "+33 6 12 34 56 78",
                        "items" to listOf(
                            OrderLineItem(prodIds[2], "Signature Gift Hamper", 1, 54.00).toMap()
                        ),
                        "totalAmount" to 54.00,
                        "status" to "shipped",
                        "paymentMethod" to "mtn_momo",
                        "paymentStatus" to "pending",
                        "paymentReference" to "MOMO-3109",
                        "notes" to "Inclure carte cadeau",
                        "channel" to "sandbox",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "ownerId" to uid,
                        "orderNumber" to "WP-2045",
                        "customerId" to custIds[3],
                        "customerName" to "Kenji Takahashi",
                        "customerPhone" to "+81 90 1234 5678",
                        "items" to listOf(
                            OrderLineItem(prodIds[0], "Single-Origin Espresso Roast", 3, 19.50).toMap()
                        ),
                        "totalAmount" to 58.50,
                        "status" to "delivered",
                        "paymentMethod" to "moov_money",
                        "paymentStatus" to "paid",
                        "paymentReference" to "MOOV-7410",
                        "notes" to "",
                        "channel" to "sandbox",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                orders.forEachIndexed { idx, ord ->
                    batch.set(wsRef.collection("orders").document(orderIds[idx]), ord)
                }

                val rules = listOf(
                    mapOf(
                        "ownerId" to uid,
                        "name" to "Catalog & Menu Inquiry",
                        "triggerType" to "keyword",
                        "keywords" to listOf("menu", "catalog", "price", "products"),
                        "matchMode" to "contains",
                        "responseText" to "Our top picks today: Single-Origin Espresso (\$19.50), Artisan Croissant Box (\$24.00), and Signature Gift Hamper (\$54.00). Let us know what you'd like to order!",
                        "category" to "Sales",
                        "active" to true,
                        "hitCount" to 42L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "ownerId" to uid,
                        "name" to "Opening Hours FAQ",
                        "triggerType" to "faq",
                        "keywords" to listOf("hours", "open", "schedule", "closing"),
                        "matchMode" to "contains",
                        "responseText" to "We are open Monday to Saturday from 08:00 to 20:00. Same-day local delivery is available for orders placed before 15:00!",
                        "category" to "Store Info",
                        "active" to true,
                        "hitCount" to 29L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "ownerId" to uid,
                        "name" to "Delivery & Shipping FAQ",
                        "triggerType" to "faq",
                        "keywords" to listOf("delivery", "shipping", "deliver", "track"),
                        "matchMode" to "contains",
                        "responseText" to "Local express delivery takes 45-90 minutes, and nationwide courier shipping arrives within 1-2 business days.",
                        "category" to "Logistics",
                        "active" to true,
                        "hitCount" to 18L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "ownerId" to uid,
                        "name" to "Wholesale & Bulk Orders",
                        "triggerType" to "keyword",
                        "keywords" to listOf("wholesale", "bulk", "catering", "corporate"),
                        "matchMode" to "contains",
                        "responseText" to "We offer 15% volume pricing on catering & corporate orders of 5+ boxes. Share your event date and headcount and our team will prepare a quote!",
                        "category" to "B2B",
                        "active" to true,
                        "hitCount" to 11L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "ownerId" to uid,
                        "name" to "Allergen & Dietary Info",
                        "triggerType" to "faq",
                        "keywords" to listOf("gluten", "vegan", "allergen", "dairy"),
                        "matchMode" to "contains",
                        "responseText" to "All our espresso & cold brew drinks can be prepared dairy-free. Croissants contain wheat and French butter.",
                        "category" to "Dietary",
                        "active" to true,
                        "hitCount" to 7L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                rules.forEachIndexed { idx, r ->
                    batch.set(wsRef.collection("automations").document(ruleIds[idx]), r)
                }

                val conversations = listOf(
                    Triple(
                        convIds[0],
                        mapOf(
                            "ownerId" to uid,
                            "customerId" to custIds[0],
                            "customerName" to "Nadia Benali",
                            "customerPhone" to "+1 415 890 1122",
                            "status" to "open",
                            "unreadCount" to 2L,
                            "lastMessage" to "Can we add 1 more croissant box to order WP-2048?",
                            "lastMessageSender" to "customer",
                            "aiSuggestedReply" to "Hi Nadia! Of course — I can add 1 more Artisan Croissant Box (\$24.00) to order WP-2048 before our 10:00 AM dispatch.",
                            "assignedRule" to "",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        listOf(
                            Triple("customer", "Hi! Checking on our morning delivery WP-2048.", ""),
                            Triple("agent", "Good morning Nadia! Your espresso beans & croissants are being packed right now.", ""),
                            Triple("customer", "Can we add 1 more croissant box to order WP-2048?", "")
                        )
                    ),
                    Triple(
                        convIds[1],
                        mapOf(
                            "ownerId" to uid,
                            "customerId" to custIds[1],
                            "customerName" to "Marcus Vance",
                            "customerPhone" to "+44 7700 900123",
                            "status" to "automated",
                            "unreadCount" to 0L,
                            "lastMessage" to "We offer 15% volume pricing on catering & corporate orders of 5+ boxes.",
                            "lastMessageSender" to "automation",
                            "assignedRule" to "Wholesale & Bulk Orders",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        listOf(
                            Triple("customer", "Do you offer catering discounts for 30 guests?", ""),
                            Triple("automation", "We offer 15% volume pricing on catering & corporate orders of 5+ boxes. Share your event date and headcount and our team will prepare a quote!", "Wholesale & Bulk Orders")
                        )
                    ),
                    Triple(
                        convIds[2],
                        mapOf(
                            "ownerId" to uid,
                            "customerId" to custIds[2],
                            "customerName" to "Elena Rostova",
                            "customerPhone" to "+33 6 12 34 56 78",
                            "status" to "resolved",
                            "unreadCount" to 0L,
                            "lastMessage" to "Thank you so much! Looking forward to the gift hamper.",
                            "lastMessageSender" to "customer",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        listOf(
                            Triple("customer", "When does courier shipping arrive?", ""),
                            Triple("automation", "Local express delivery takes 45-90 minutes, and nationwide courier shipping arrives within 1-2 business days.", "Delivery & Shipping FAQ"),
                            Triple("customer", "Thank you so much! Looking forward to the gift hamper.", "")
                        )
                    ),
                    Triple(
                        convIds[3],
                        mapOf(
                            "ownerId" to uid,
                            "customerId" to custIds[4],
                            "customerName" to "Sofia Morales",
                            "customerPhone" to "+34 611 223 344",
                            "status" to "open",
                            "unreadCount" to 1L,
                            "lastMessage" to "Do you have gift boxes with tea instead of coffee?",
                            "lastMessageSender" to "customer",
                            "aiSuggestedReply" to "Hi Sofia! Yes, our Signature Gift Hamper (\$54.00) includes curated specialty tea and can be customized without coffee.",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        listOf(
                            Triple("customer", "Do you have gift boxes with tea instead of coffee?", "")
                        )
                    )
                )

                conversations.forEach { (cid, convData, msgs) ->
                    val convRef = wsRef.collection("conversations").document(cid)
                    batch.set(convRef, convData)
                    msgs.forEach { (sender, txt, ruleName) ->
                        val msgMap = buildMap<String, Any> {
                            put("ownerId", uid)
                            put("senderType", sender)
                            put("text", txt)
                            if (ruleName.isNotEmpty()) put("ruleMatched", ruleName)
                            put("createdAt", FieldValue.serverTimestamp())
                        }
                        batch.set(convRef.collection("messages").document(), msgMap)
                    }
                }
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, wsRef.path, auth)
            throw e
        }
    }
}
