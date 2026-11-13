package com.example.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.ui.WhatsProSection

enum class AppLanguage(val code: String, val label: String) {
    FR("FR", "Français"),
    EN("EN", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FR
    }
}

/**
 * Modular facade holding domain-grouped string bundles.
 * Keeping the primary constructor to 12 group parameters prevents Dalvik/ART's 255-register
 * parameter limit (`VerifyError`) as localization grows.
 */
data class AppStrings(
    val shell: ShellStrings,
    val auth: AuthStrings,
    val dashboard: DashboardStrings,
    val catalog: CatalogStrings,
    val orders: OrdersStrings,
    val customers: CustomerStrings,
    val inbox: InboxStrings,
    val automation: AutomationStrings,
    val ai: AiStrings,
    val company: CompanyStrings,
    val searchFilter: SearchFilterStrings,
    val common: CommonStrings
) {
    // Brand & Shell
    val suiteTagline: String get() = shell.suiteTagline
    val whatsappNotConnectedTitle: String get() = shell.whatsappNotConnectedTitle
    val phase2Badge: String get() = shell.phase2Badge
    val whatsappFoundationDesc: String get() = shell.whatsappFoundationDesc
    val sandboxChip: String get() = shell.sandboxChip
    val cloudApiOffSuffix: String get() = shell.cloudApiOffSuffix
    val loadingWorkspace: String get() = shell.loadingWorkspace
    val workspaceError: String get() = shell.workspaceError
    val languageLabel: String get() = shell.languageLabel
    val justNow: String get() = shell.justNow

    // Sections
    val sectionDashboard: String get() = shell.sectionDashboard
    val sectionInbox: String get() = shell.sectionInbox
    val sectionCustomers: String get() = shell.sectionCustomers
    val sectionCatalog: String get() = shell.sectionCatalog
    val sectionOrders: String get() = shell.sectionOrders
    val sectionAutomation: String get() = shell.sectionAutomation
    val sectionAiAssistant: String get() = shell.sectionAiAssistant
    val sectionCompanyBilling: String get() = shell.sectionCompanyBilling

    // Auth & Onboarding
    val authTabSignIn: String get() = auth.authTabSignIn
    val authTabRegister: String get() = auth.authTabRegister
    val authWelcomeTitle: String get() = auth.authWelcomeTitle
    val authRegisterTitle: String get() = auth.authRegisterTitle
    val authWelcomeDesc: String get() = auth.authWelcomeDesc
    val authRegisterDesc: String get() = auth.authRegisterDesc
    val authFeatureIsolation: String get() = auth.authFeatureIsolation
    val authFeatureAutomation: String get() = auth.authFeatureAutomation
    val authFeatureFoundation: String get() = auth.authFeatureFoundation
    val authBtnSignIn: String get() = auth.authBtnSignIn
    val authBtnRegister: String get() = auth.authBtnRegister
    val onboardingSwitchAccount: String get() = auth.onboardingSwitchAccount
    val onboardingTitle: String get() = auth.onboardingTitle
    val onboardingSubtitlePrefix: String get() = auth.onboardingSubtitlePrefix
    val onboardingCompanyLabel: String get() = auth.onboardingCompanyLabel
    val onboardingIndustryLabel: String get() = auth.onboardingIndustryLabel
    val onboardingCurrencyLabel: String get() = auth.onboardingCurrencyLabel
    val onboardingDemoTitle: String get() = auth.onboardingDemoTitle
    val onboardingDemoSubtitle: String get() = auth.onboardingDemoSubtitle
    val onboardingLaunchBtn: String get() = auth.onboardingLaunchBtn

    // Dashboard
    val dashSummaryTitle: String get() = dashboard.dashSummaryTitle
    val dashSummarySubtitle: String get() = dashboard.dashSummarySubtitle
    val dashGreenKpisBadge: String get() = dashboard.dashGreenKpisBadge
    val dashTotalCustomers: String get() = dashboard.dashTotalCustomers
    val dashTotalOrders: String get() = dashboard.dashTotalOrders
    val dashSalesStats: String get() = dashboard.dashSalesStats
    val dashConversations: String get() = dashboard.dashConversations
    val dashAutomationStats: String get() = dashboard.dashAutomationStats
    val dashAutomationHits: String get() = dashboard.dashAutomationHits
    val dashValidOrdersSuffix: String get() = dashboard.dashValidOrdersSuffix
    val dashAvgPrefix: String get() = dashboard.dashAvgPrefix
    val dashSalesRevenueNote: String get() = dashboard.dashSalesRevenueNote
    val dashActiveSuffix: String get() = dashboard.dashActiveSuffix
    val dashDeliveredSuffix: String get() = dashboard.dashDeliveredSuffix
    val dashDoneSuffix: String get() = dashboard.dashDoneSuffix
    val dashInProgressSuffix: String get() = dashboard.dashInProgressSuffix
    val dashRulesSuffix: String get() = dashboard.dashRulesSuffix
    val dashAutoSuffix: String get() = dashboard.dashAutoSuffix
    val dashActiveThreadsSuffix: String get() = dashboard.dashActiveThreadsSuffix
    val dashChartTitle: String get() = dashboard.dashChartTitle
    val dashChartSubtitleFormat: (Long, Int) -> String get() = dashboard.dashChartSubtitleFormat
    val dashChartEmpty: String get() = dashboard.dashChartEmpty
    val dashTelemetryBadge: String get() = dashboard.dashTelemetryBadge
    val dashRecentConversations: String get() = dashboard.dashRecentConversations
    val dashOpenInbox: String get() = dashboard.dashOpenInbox
    val dashNoConversations: String get() = dashboard.dashNoConversations
    val dashRecentOrders: String get() = dashboard.dashRecentOrders
    val dashAllOrders: String get() = dashboard.dashAllOrders
    val dashNoOrders: String get() = dashboard.dashNoOrders
    val dashAiReady: String get() = dashboard.dashAiReady
    val dashAiPaused: String get() = dashboard.dashAiPaused
    val dashItemsSuffix: String get() = dashboard.dashItemsSuffix

    // Catalog
    val catalogTitle: String get() = catalog.catalogTitle
    val catalogSubtitleFormat: (Int) -> String get() = catalog.catalogSubtitleFormat
    val catalogAddProduct: String get() = catalog.catalogAddProduct
    val catalogSearchPlaceholder: String get() = catalog.catalogSearchPlaceholder
    val catalogAllCategory: String get() = catalog.catalogAllCategory
    val catalogEmptyTitle: String get() = catalog.catalogEmptyTitle
    val catalogEmptySubtitle: String get() = catalog.catalogEmptySubtitle
    val catalogStockPrefix: String get() = catalog.catalogStockPrefix
    val catalogLocalPhotoBadge: String get() = catalog.catalogLocalPhotoBadge
    val catalogEditTitle: String get() = catalog.catalogEditTitle
    val catalogProductNameLabel: String get() = catalog.catalogProductNameLabel
    val catalogDescriptionLabel: String get() = catalog.catalogDescriptionLabel
    val catalogPriceLabelPrefix: String get() = catalog.catalogPriceLabelPrefix
    val catalogStockLabel: String get() = catalog.catalogStockLabel
    val catalogCategoryLabel: String get() = catalog.catalogCategoryLabel
    val catalogSkuLabel: String get() = catalog.catalogSkuLabel
    val catalogPhotoSectionLabel: String get() = catalog.catalogPhotoSectionLabel
    val catalogPresetCoffee: String get() = catalog.catalogPresetCoffee
    val catalogPresetBakery: String get() = catalog.catalogPresetBakery
    val catalogPresetGift: String get() = catalog.catalogPresetGift
    val catalogPickLocalPhoto: String get() = catalog.catalogPickLocalPhoto
    val catalogLocalPhotoPicked: String get() = catalog.catalogLocalPhotoPicked
    val catalogCopyingPhoto: String get() = catalog.catalogCopyingPhoto
    val catalogResetPhoto: String get() = catalog.catalogResetPhoto
    val catalogLocalPhotoHint: String get() = catalog.catalogLocalPhotoHint

    // Orders
    val ordersTitle: String get() = orders.ordersTitle
    val ordersSubtitleFormat: (Int, String) -> String get() = orders.ordersSubtitleFormat
    val ordersNewOrder: String get() = orders.ordersNewOrder
    val ordersEmptyTitle: String get() = orders.ordersEmptyTitle
    val ordersEmptySubtitle: String get() = orders.ordersEmptySubtitle
    val ordersTotalLabel: String get() = orders.ordersTotalLabel
    val ordersNotePrefix: String get() = orders.ordersNotePrefix
    val ordersMarkPrefix: String get() = orders.ordersMarkPrefix
    val ordersCancelBtn: String get() = orders.ordersCancelBtn
    val ordersConfirmPaymentBtn: String get() = orders.ordersConfirmPaymentBtn
    val ordersDialogTitle: String get() = orders.ordersDialogTitle
    val ordersSelectCustomer: String get() = orders.ordersSelectCustomer
    val ordersSelectProducts: String get() = orders.ordersSelectProducts
    val ordersPaymentMethodLabel: String get() = orders.ordersPaymentMethodLabel
    val ordersPaymentRefLabel: String get() = orders.ordersPaymentRefLabel
    val ordersMarkPaidNowLabel: String get() = orders.ordersMarkPaidNowLabel
    val ordersMerchantNumberHintPrefix: String get() = orders.ordersMerchantNumberHintPrefix
    val ordersNotesLabel: String get() = orders.ordersNotesLabel
    val ordersPlaceOrderBtn: String get() = orders.ordersPlaceOrderBtn
    val ordersStatusAll: String get() = orders.ordersStatusAll
    val paymentStatusPaid: String get() = orders.paymentStatusPaid
    val paymentStatusPending: String get() = orders.paymentStatusPending
    val paymentRefPrefix: String get() = orders.paymentRefPrefix

    // Customers
    val customersTitle: String get() = customers.customersTitle
    val customersSubtitleFormat: (Int) -> String get() = customers.customersSubtitleFormat
    val customersAdd: String get() = customers.customersAdd
    val customersSearchPlaceholder: String get() = customers.customersSearchPlaceholder
    val customersEmptyTitle: String get() = customers.customersEmptyTitle
    val customersEmptySubtitle: String get() = customers.customersEmptySubtitle
    val customersChatBtn: String get() = customers.customersChatBtn
    val customersOrdersSpentFormat: (Long, String) -> String get() = customers.customersOrdersSpentFormat
    val customersAddTitle: String get() = customers.customersAddTitle
    val customersEditTitle: String get() = customers.customersEditTitle
    val customersNameLabel: String get() = customers.customersNameLabel
    val customersPhoneLabel: String get() = customers.customersPhoneLabel
    val customersEmailLabel: String get() = customers.customersEmailLabel
    val customersCompanyLabel: String get() = customers.customersCompanyLabel
    val customersTagsLabel: String get() = customers.customersTagsLabel
    val customersNotesLabel: String get() = customers.customersNotesLabel

    // Inbox
    val inboxSearchPlaceholder: String get() = inbox.inboxSearchPlaceholder
    val inboxSelectTitle: String get() = inbox.inboxSelectTitle
    val inboxSelectSubtitle: String get() = inbox.inboxSelectSubtitle
    val inboxNoMatchTitle: String get() = inbox.inboxNoMatchTitle
    val inboxNoMatchSubtitle: String get() = inbox.inboxNoMatchSubtitle
    val inboxSandboxHeaderSuffix: String get() = inbox.inboxSandboxHeaderSuffix
    val inboxReopen: String get() = inbox.inboxReopen
    val inboxResolve: String get() = inbox.inboxResolve
    val inboxAiDraftTitle: String get() = inbox.inboxAiDraftTitle
    val inboxUseDraft: String get() = inbox.inboxUseDraft
    val inboxGeneratingAi: String get() = inbox.inboxGeneratingAi
    val inboxReplyAsAgent: String get() = inbox.inboxReplyAsAgent
    val inboxSimulateCustomer: String get() = inbox.inboxSimulateCustomer
    val inboxSimulateHint: String get() = inbox.inboxSimulateHint
    val inboxReplyHintPrefix: String get() = inbox.inboxReplyHintPrefix
    val inboxAutoReplyPrefix: String get() = inbox.inboxAutoReplyPrefix

    // Automation
    val autoTitle: String get() = automation.autoTitle
    val autoSubtitle: String get() = automation.autoSubtitle
    val autoAddRule: String get() = automation.autoAddRule
    val autoPipelineTitle: String get() = automation.autoPipelineTitle
    val autoPipelineSubtitle: String get() = automation.autoPipelineSubtitle
    val autoStepWelcome: String get() = automation.autoStepWelcome
    val autoStepKeywords: String get() = automation.autoStepKeywords
    val autoStepFaqs: String get() = automation.autoStepFaqs
    val autoStepAi: String get() = automation.autoStepAi
    val autoActiveLabel: String get() = automation.autoActiveLabel
    val autoOffLabel: String get() = automation.autoOffLabel
    val autoReadyLabel: String get() = automation.autoReadyLabel
    val autoWelcomeTitle: String get() = automation.autoWelcomeTitle
    val autoWelcomeSubtitle: String get() = automation.autoWelcomeSubtitle
    val autoGreetingLabel: String get() = automation.autoGreetingLabel
    val autoSaveWelcome: String get() = automation.autoSaveWelcome
    val autoTabKeywords: String get() = automation.autoTabKeywords
    val autoTabFaqs: String get() = automation.autoTabFaqs
    val autoEmptyKeywordTitle: String get() = automation.autoEmptyKeywordTitle
    val autoEmptyFaqTitle: String get() = automation.autoEmptyFaqTitle
    val autoEmptySubtitle: String get() = automation.autoEmptySubtitle
    val autoMatchLabel: String get() = automation.autoMatchLabel
    val autoTriggersLabel: String get() = automation.autoTriggersLabel
    val autoNewRuleTitle: String get() = automation.autoNewRuleTitle
    val autoEditRuleTitle: String get() = automation.autoEditRuleTitle
    val autoRuleNameLabel: String get() = automation.autoRuleNameLabel
    val autoTriggerKeywordLabel: String get() = automation.autoTriggerKeywordLabel
    val autoTriggerFaqLabel: String get() = automation.autoTriggerFaqLabel
    val autoMatchContains: String get() = automation.autoMatchContains
    val autoMatchExact: String get() = automation.autoMatchExact
    val autoKeywordsLabel: String get() = automation.autoKeywordsLabel
    val autoReplyMessageLabel: String get() = automation.autoReplyMessageLabel
    val autoSaveRuleBtn: String get() = automation.autoSaveRuleBtn
    val autoVisualStatsActiveLabel: String get() = automation.autoVisualStatsActiveLabel
    val autoVisualStatsHitsLabel: String get() = automation.autoVisualStatsHitsLabel
    val autoVisualSandboxPill: String get() = automation.autoVisualSandboxPill
    val autoTesterTitle: String get() = automation.autoTesterTitle
    val autoTesterHint: String get() = automation.autoTesterHint
    val autoTesterNoMatch: String get() = automation.autoTesterNoMatch
    val autoTesterAiFallback: String get() = automation.autoTesterAiFallback
    val autoTesterMatchedStagePrefix: String get() = automation.autoTesterMatchedStagePrefix
    val autoWelcomePreviewTitle: String get() = automation.autoWelcomePreviewTitle
    val autoWelcomeVarsLabel: String get() = automation.autoWelcomeVarsLabel
    val autoWelcomePresetsLabel: String get() = automation.autoWelcomePresetsLabel
    val autoWelcomeUnsavedBadge: String get() = automation.autoWelcomeUnsavedBadge
    val autoWelcomeDisabledNotice: String get() = automation.autoWelcomeDisabledNotice
    val autoIfTriggerLabel: String get() = automation.autoIfTriggerLabel
    val autoThenReplyLabel: String get() = automation.autoThenReplyLabel
    val autoQuickTemplatesTitle: String get() = automation.autoQuickTemplatesTitle
    val autoKeywordChipHint: String get() = automation.autoKeywordChipHint
    val autoAddKeywordBtn: String get() = automation.autoAddKeywordBtn
    val autoPreviewBubbleLabel: String get() = automation.autoPreviewBubbleLabel

    // AI
    val aiTitle: String get() = ai.aiTitle
    val aiSubtitleFormat: (Int, Int) -> String get() = ai.aiSubtitleFormat
    val aiEnableTitle: String get() = ai.aiEnableTitle
    val aiEnableDesc: String get() = ai.aiEnableDesc
    val aiToneLabel: String get() = ai.aiToneLabel
    val aiToneFriendly: String get() = ai.aiToneFriendly
    val aiToneProfessional: String get() = ai.aiToneProfessional
    val aiToneConcise: String get() = ai.aiToneConcise
    val aiLanguageLabel: String get() = ai.aiLanguageLabel
    val aiInstructionsLabel: String get() = ai.aiInstructionsLabel
    val aiSaveConfigBtn: String get() = ai.aiSaveConfigBtn
    val aiSandboxTitle: String get() = ai.aiSandboxTitle
    val aiSandboxDesc: String get() = ai.aiSandboxDesc
    val aiSandboxInputLabel: String get() = ai.aiSandboxInputLabel
    val aiSandboxTestBtn: String get() = ai.aiSandboxTestBtn
    val aiSandboxThinking: String get() = ai.aiSandboxThinking

    // Company
    val companyTitle: String get() = company.companyTitle
    val companySubtitleFormat: (String, String) -> String get() = company.companySubtitleFormat
    val companyWaCardTitle: String get() = company.companyWaCardTitle
    val companyWaCardStatus: String get() = company.companyWaCardStatus
    val companyWaCardDesc: String get() = company.companyWaCardDesc
    val companyAppLanguageTitle: String get() = company.companyAppLanguageTitle
    val companyAppLanguageDesc: String get() = company.companyAppLanguageDesc
    val companyDetailsTitle: String get() = company.companyDetailsTitle
    val companyNameLabel: String get() = company.companyNameLabel
    val companyIndustryLabel: String get() = company.companyIndustryLabel
    val companyEmailLabel: String get() = company.companyEmailLabel
    val companyPhoneLabel: String get() = company.companyPhoneLabel
    val companyAddressLabel: String get() = company.companyAddressLabel
    val companyHoursLabel: String get() = company.companyHoursLabel
    val companyCurrencyLabel: String get() = company.companyCurrencyLabel
    val companyMobileMoneyTitle: String get() = company.companyMobileMoneyTitle
    val companyMobileMoneyDesc: String get() = company.companyMobileMoneyDesc
    val companyMobileMoneyNumberLabel: String get() = company.companyMobileMoneyNumberLabel
    val companySaveProfileBtn: String get() = company.companySaveProfileBtn
    val companyPlanTitle: String get() = company.companyPlanTitle
    val companyContactsUsedLabel: String get() = company.companyContactsUsedLabel
    val companyAutomationRunsLabel: String get() = company.companyAutomationRunsLabel
    val companyMonthlySuffix: String get() = company.companyMonthlySuffix
    val companySeedDemoBtn: String get() = company.companySeedDemoBtn
    val companySignOutBtn: String get() = company.companySignOutBtn

    // Search & Filter
    val searchClearDesc: String get() = searchFilter.searchClearDesc
    val searchFilterIconDesc: String get() = searchFilter.searchFilterIconDesc
    val searchResultsCountFormat: (Int) -> String get() = searchFilter.searchResultsCountFormat
    val searchResetFilters: String get() = searchFilter.searchResetFilters
    val filterCategoryTitle: String get() = searchFilter.filterCategoryTitle
    val filterAvailabilityTitle: String get() = searchFilter.filterAvailabilityTitle
    val filterOnlyActive: String get() = searchFilter.filterOnlyActive
    val filterLowStock: String get() = searchFilter.filterLowStock
    val filterSegmentTitle: String get() = searchFilter.filterSegmentTitle
    val filterSortTitle: String get() = searchFilter.filterSortTitle
    val filterSortNameAsc: String get() = searchFilter.filterSortNameAsc
    val filterSortPriceAsc: String get() = searchFilter.filterSortPriceAsc
    val filterSortSpentDesc: String get() = searchFilter.filterSortSpentDesc

    // Common
    val save: String get() = common.save
    val cancel: String get() = common.cancel
    val edit: String get() = common.edit
    val delete: String get() = common.delete
    val send: String get() = common.send
    val back: String get() = common.back

    fun sectionLabel(section: WhatsProSection): String = when (section) {
        WhatsProSection.DASHBOARD -> sectionDashboard
        WhatsProSection.INBOX -> sectionInbox
        WhatsProSection.CUSTOMERS -> sectionCustomers
        WhatsProSection.CATALOG -> sectionCatalog
        WhatsProSection.ORDERS -> sectionOrders
        WhatsProSection.AUTOMATION -> sectionAutomation
        WhatsProSection.AI_ASSISTANT -> sectionAiAssistant
        WhatsProSection.COMPANY_BILLING -> sectionCompanyBilling
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.FR }
val LocalSetAppLanguage = staticCompositionLocalOf<(AppLanguage) -> Unit> { {} }
val LocalAppStrings = compositionLocalOf { FrenchStrings }

@Composable
fun localizedStatusLabel(status: String): String {
    val lang = LocalAppLanguage.current
    if (lang == AppLanguage.EN) {
        return status.replaceFirstChar { it.uppercase() }
    }
    return when (status.lowercase()) {
        "all" -> "Tout"
        "pending" -> "En attente"
        "confirmed" -> "Confirmée"
        "preparing" -> "En préparation"
        "shipped" -> "Expédiée"
        "delivered" -> "Livrée"
        "cancelled" -> "Annulée"
        "open" -> "Ouverte"
        "automated" -> "Automatisée"
        "resolved" -> "Résolue"
        "vip" -> "VIP"
        "wholesale" -> "Grossiste"
        "regular" -> "Régulier"
        "lead" -> "Prospect"
        "paid" -> "Payé"
        "customer" -> "Client"
        "agent" -> "Agent"
        "automation" -> "Automatisation"
        "ai" -> "IA"
        else -> status.replaceFirstChar { it.uppercase() }
    }
}
