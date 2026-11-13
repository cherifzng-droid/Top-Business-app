package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class WestAfricaPaymentMethod(
    val id: String,
    val displayName: String,
    val regionNote: String,
    val accentColorHex: Long
) {
    WAVE(
        id = "wave",
        displayName = "Wave",
        regionNote = "Sénégal, Côte d'Ivoire, Mali, Burkina Faso",
        accentColorHex = 0xFF1DC4FF
    ),
    ORANGE_MONEY(
        id = "orange_money",
        displayName = "Orange Money",
        regionNote = "Sénégal, CI, Mali, Burkina, Guinée, Niger",
        accentColorHex = 0xFFFF7900
    ),
    MTN_MOMO(
        id = "mtn_momo",
        displayName = "MTN MoMo",
        regionNote = "Côte d'Ivoire, Bénin, Ghana, Guinée, Nigeria",
        accentColorHex = 0xFFFFCC00
    ),
    MOOV_MONEY(
        id = "moov_money",
        displayName = "Moov Money",
        regionNote = "Bénin, Togo, CI, Burkina, Mali, Niger",
        accentColorHex = 0xFF005BAC
    ),
    MIXX_BY_YAS(
        id = "mixx_by_yas",
        displayName = "Mixx by Yas",
        regionNote = "Togo (TMoney), Sénégal (Free Money)",
        accentColorHex = 0xFF0066FF
    ),
    CASH_ON_DELIVERY(
        id = "cash_on_delivery",
        displayName = "Espèces à la livraison",
        regionNote = "Paiement direct à la réception",
        accentColorHex = 0xFF00A884
    );

    companion object {
        fun fromId(id: String): WestAfricaPaymentMethod =
            entries.firstOrNull { it.id == id } ?: WAVE
    }
}

@IgnoreExtraProperties
data class WorkspaceProfile(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val companyName: String = "",
    val industry: String = "Boutique & Commerce",
    val currency: String = "XOF",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val businessHours: String = "Lun-Sam 08:00 - 20:00",
    val planTier: String = "growth",
    val whatsappStatus: String = "not_connected",
    val welcomeEnabled: Boolean = true,
    val welcomeMessage: String = "",
    val aiEnabled: Boolean = true,
    val aiTone: String = "friendly",
    val aiLanguage: String = "Français",
    val aiInstructions: String = "",
    val acceptedPaymentMethods: List<String> = listOf(
        "wave",
        "orange_money",
        "mtn_momo",
        "moov_money",
        "cash_on_delivery"
    ),
    val mobileMoneyNumber: String = "",
    val seededDemoData: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Customer(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val segment: String = "regular",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val totalOrders: Long = 0L,
    val totalSpent: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "Général",
    val sku: String = "",
    val price: Double = 0.0,
    val stock: Long = 0L,
    val active: Boolean = true,
    val imageKey: String = "coffee",
    val imageUri: String = "",
    val localImagePath: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class OrderLineItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Long = 1L,
    val unitPrice: Double = 0.0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "productId" to productId,
        "productName" to productName,
        "quantity" to quantity,
        "unitPrice" to unitPrice
    )
}

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val orderNumber: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<OrderLineItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "pending",
    val paymentMethod: String = "wave",
    val paymentStatus: String = "pending",
    val paymentReference: String = "",
    val notes: String = "",
    val channel: String = "sandbox",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val status: String = "open",
    val unreadCount: Long = 0L,
    val lastMessage: String = "",
    val lastMessageSender: String = "customer",
    val aiSuggestedReply: String = "",
    val assignedRule: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val senderType: String = "customer",
    val text: String = "",
    val ruleMatched: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AutomationRule(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val triggerType: String = "keyword",
    val keywords: List<String> = emptyList(),
    val matchMode: String = "contains",
    val responseText: String = "",
    val category: String = "Général",
    val active: Boolean = true,
    val hitCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
