package com.example.data

import android.util.Log
import com.example.model.AutomationRule
import com.example.model.ChatMessage
import com.example.model.Product
import com.example.model.WorkspaceProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

class AiAssistantService {

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    suspend fun generateReplySuggestion(
        workspace: WorkspaceProfile,
        products: List<Product>,
        rules: List<AutomationRule>,
        customerName: String,
        recentMessages: List<ChatMessage>
    ): String {
        val prompt = buildContextPrompt(
            workspace = workspace,
            products = products,
            rules = rules,
            customerName = customerName,
            recentMessages = recentMessages,
            customerQuery = null
        )

        return try {
            val response = withTimeout(TIMEOUT_MS) {
                model.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (text.isNullOrBlank()) {
                throw IllegalStateException("AI assistant returned an empty reply.")
            }
            text
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI reply suggestion timed out", e)
            throw IllegalStateException("AI request timed out after 60 seconds. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI reply suggestion failed", e)
            throw IllegalStateException(e.localizedMessage ?: "AI reply generation failed.")
        }
    }

    fun streamSandboxResponse(
        workspace: WorkspaceProfile,
        products: List<Product>,
        rules: List<AutomationRule>,
        customerQuery: String
    ): Flow<String> = flow {
        val prompt = buildContextPrompt(
            workspace = workspace,
            products = products,
            rules = rules,
            customerName = "Test Customer",
            recentMessages = emptyList(),
            customerQuery = customerQuery
        )

        val accumulated = StringBuilder()
        try {
            withTimeout(TIMEOUT_MS) {
                model.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("AI assistant returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI sandbox streaming timed out", e)
            throw IllegalStateException("AI sandbox timed out after 60 seconds.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI sandbox streaming failed", e)
            throw IllegalStateException(e.localizedMessage ?: "AI assistant failed to respond.")
        }
    }

    private fun buildContextPrompt(
        workspace: WorkspaceProfile,
        products: List<Product>,
        rules: List<AutomationRule>,
        customerName: String,
        recentMessages: List<ChatMessage>,
        customerQuery: String?
    ): String {
        val catalogSnippet = products.filter { it.active }.take(12).joinToString("\n") {
            val priceFormatted = if (workspace.currency.equals("XOF", ignoreCase = true)) {
                "${kotlin.math.round(it.price).toLong()} XOF"
            } else {
                "${"%.2f".format(it.price)} ${workspace.currency}"
            }
            "- ${it.name} (${it.category}): $priceFormatted | Stock: ${it.stock} | ${it.description}"
        }.ifBlank { "No active products listed yet." }

        val faqSnippet = rules.filter { it.active }.take(10).joinToString("\n") {
            "- [${it.triggerType}] ${it.name} (${it.keywords.joinToString(", ")}): ${it.responseText}"
        }.ifBlank { "No active FAQ rules." }

        val historySnippet = if (recentMessages.isNotEmpty()) {
            recentMessages.takeLast(8).joinToString("\n") {
                "${it.senderType.uppercase()}: ${it.text}"
            }
        } else {
            "CUSTOMER: ${customerQuery.orEmpty()}"
        }

        val paymentSnippet = workspace.acceptedPaymentMethods.joinToString(", ").ifBlank { "wave, orange_money, cash_on_delivery" }

        return """
            You are the WhatsPro AI customer assistant for "${workspace.companyName}" (${workspace.industry}).
            Language: ${workspace.aiLanguage}
            Tone: ${workspace.aiTone}
            Business Hours: ${workspace.businessHours}
            Currency: ${workspace.currency}
            Accepted Payment Methods: $paymentSnippet (Merchant Mobile Money #: ${workspace.mobileMoneyNumber.ifBlank { "configured in settings" }})
            Extra Business Instructions: ${workspace.aiInstructions.ifBlank { "Be helpful, accurate, and concise." }}

            Active Product Catalog:
            $catalogSnippet

            Active FAQs & Business Policies:
            $faqSnippet

            Conversation with $customerName:
            $historySnippet

            Write the next business reply directly as the customer support assistant (no markdown headers or explanations, ready to send to the customer in ${workspace.aiLanguage}).
        """.trimIndent()
    }

    private companion object {
        const val TAG = "AiAssistantService"
        const val TIMEOUT_MS = 60_000L
    }
}
