package com.example.model

enum class AutomationMatchStage {
    NONE,
    WELCOME,
    KEYWORD,
    FAQ,
    AI_FALLBACK
}

data class AutomationMatchResult(
    val stage: AutomationMatchStage,
    val matchedRule: AutomationRule? = null,
    val matchedKeyword: String? = null,
    val replyText: String? = null
)

object AutomationEngine {

    val defaultGreetings = listOf(
        "hi",
        "hello",
        "hey",
        "bonjour",
        "salut",
        "bonsoir",
        "hola",
        "salam",
        "start"
    )

    fun resolveTemplateVariables(template: String, workspace: WorkspaceProfile): String {
        if (template.isBlank()) return ""
        val company = workspace.companyName.ifBlank { "WhatsPro" }
        val hours = workspace.businessHours.ifBlank { "08:00 - 20:00" }
        val currency = workspace.currency.ifBlank { "XOF" }
        val phone = workspace.phone.ifBlank { "+221 77 000 00 00" }
        return template
            .replace("{entreprise}", company, ignoreCase = true)
            .replace("{company}", company, ignoreCase = true)
            .replace("{horaires}", hours, ignoreCase = true)
            .replace("{hours}", hours, ignoreCase = true)
            .replace("{devise}", currency, ignoreCase = true)
            .replace("{currency}", currency, ignoreCase = true)
            .replace("{telephone}", phone, ignoreCase = true)
            .replace("{phone}", phone, ignoreCase = true)
    }

    fun evaluate(
        incomingText: String,
        workspace: WorkspaceProfile,
        rules: List<AutomationRule>
    ): AutomationMatchResult {
        val normalized = incomingText.trim().lowercase()
        if (normalized.isEmpty()) {
            return AutomationMatchResult(stage = AutomationMatchStage.NONE)
        }

        // Prioritize keyword rules first, then FAQ rules, keeping list order within each group
        val orderedRules = rules.filter { it.active }.sortedBy {
            if (it.triggerType == "keyword") 0 else 1
        }

        for (rule in orderedRules) {
            val hitKeyword = rule.keywords.firstOrNull { kw ->
                val cleanKw = kw.trim().lowercase()
                if (cleanKw.isEmpty()) {
                    false
                } else if (rule.matchMode == "exact") {
                    normalized == cleanKw
                } else {
                    normalized.contains(cleanKw)
                }
            }
            if (hitKeyword != null) {
                val stage = if (rule.triggerType == "faq") {
                    AutomationMatchStage.FAQ
                } else {
                    AutomationMatchStage.KEYWORD
                }
                return AutomationMatchResult(
                    stage = stage,
                    matchedRule = rule,
                    matchedKeyword = hitKeyword.trim(),
                    replyText = resolveTemplateVariables(rule.responseText, workspace)
                )
            }
        }

        // Greeting check for automatic Welcome Message
        if (workspace.welcomeEnabled &&
            workspace.welcomeMessage.isNotBlank() &&
            defaultGreetings.any { g ->
                normalized == g || normalized.startsWith("$g ") || normalized.startsWith("$g!") || normalized.startsWith("$g,")
            }
        ) {
            return AutomationMatchResult(
                stage = AutomationMatchStage.WELCOME,
                matchedKeyword = defaultGreetings.firstOrNull { g ->
                    normalized == g || normalized.startsWith("$g ") || normalized.startsWith("$g!") || normalized.startsWith("$g,")
                },
                replyText = resolveTemplateVariables(workspace.welcomeMessage, workspace)
            )
        }

        if (workspace.aiEnabled) {
            return AutomationMatchResult(stage = AutomationMatchStage.AI_FALLBACK)
        }

        return AutomationMatchResult(stage = AutomationMatchStage.NONE)
    }
}
