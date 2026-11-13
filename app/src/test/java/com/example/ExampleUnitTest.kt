package com.example

import com.example.model.AutomationEngine
import com.example.model.AutomationMatchStage
import com.example.model.AutomationRule
import com.example.model.Customer
import com.example.model.CustomerSortOption
import com.example.model.Product
import com.example.model.ProductSortOption
import com.example.model.RecordSearchFilter
import com.example.model.WestAfricaPaymentMethod
import com.example.model.WorkspaceProfile
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.AiStrings
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.AuthStrings
import com.example.ui.i18n.AutomationStrings
import com.example.ui.i18n.CatalogStrings
import com.example.ui.i18n.CommonStrings
import com.example.ui.i18n.CompanyStrings
import com.example.ui.i18n.CustomerStrings
import com.example.ui.i18n.DashboardStrings
import com.example.ui.i18n.EnglishStrings
import com.example.ui.i18n.FrenchStrings
import com.example.ui.i18n.InboxStrings
import com.example.ui.i18n.OrdersStrings
import com.example.ui.i18n.SearchFilterStrings
import com.example.ui.i18n.ShellStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun formatCurrency_displaysXofAndWestAfricanCurrenciesProperly() {
        val formattedXof = formatCurrency(12500.0, "XOF")
        assertTrue(formattedXof.endsWith(" XOF"))
        assertTrue(formattedXof.contains("12") && formattedXof.contains("500"))

        assertEquals("GH₵125.00", formatCurrency(125.0, "GHS"))
        val formattedNgn = formatCurrency(8500.0, "NGN")
        assertTrue(formattedNgn.startsWith("₦"))
        assertEquals("€19.90", formatCurrency(19.9, "EUR"))
    }

    @Test
    fun westAfricaPaymentMethods_resolveCorrectly() {
        assertEquals(WestAfricaPaymentMethod.WAVE, WestAfricaPaymentMethod.fromId("wave"))
        assertEquals(WestAfricaPaymentMethod.ORANGE_MONEY, WestAfricaPaymentMethod.fromId("orange_money"))
        assertEquals(WestAfricaPaymentMethod.MTN_MOMO, WestAfricaPaymentMethod.fromId("mtn_momo"))
        assertEquals(WestAfricaPaymentMethod.MOOV_MONEY, WestAfricaPaymentMethod.fromId("moov_money"))
        assertEquals(WestAfricaPaymentMethod.MIXX_BY_YAS, WestAfricaPaymentMethod.fromId("mixx_by_yas"))
    }

    @Test
    fun automationEngine_resolvesWelcomeVariablesAndMatchesKeywordRules() {
        val workspace = WorkspaceProfile(
            companyName = "Dakar Roasters",
            businessHours = "08:00 - 20:00",
            currency = "XOF",
            welcomeEnabled = true,
            welcomeMessage = "Bienvenue chez {entreprise} ({devise}) ! Horaires : {horaires}.",
            aiEnabled = true
        )
        val rules = listOf(
            AutomationRule(
                id = "r1",
                name = "Paiement Wave",
                triggerType = "keyword",
                keywords = listOf("wave", "orange money"),
                matchMode = "contains",
                responseText = "Paiement Wave accepté chez {entreprise} !",
                active = true
            ),
            AutomationRule(
                id = "r2",
                name = "Exact Code",
                triggerType = "faq",
                keywords = listOf("promo"),
                matchMode = "exact",
                responseText = "Code PROMO10 actif",
                active = true
            )
        )

        // 1. Greeting triggers WELCOME with variable substitution
        val welcomeMatch = AutomationEngine.evaluate("Bonjour !", workspace, rules)
        assertEquals(AutomationMatchStage.WELCOME, welcomeMatch.stage)
        assertEquals("Bienvenue chez Dakar Roasters (XOF) ! Horaires : 08:00 - 20:00.", welcomeMatch.replyText)

        // 2. Keyword contains triggers KEYWORD stage
        val keywordMatch = AutomationEngine.evaluate("Je veux payer par Wave svp", workspace, rules)
        assertEquals(AutomationMatchStage.KEYWORD, keywordMatch.stage)
        assertNotNull(keywordMatch.matchedRule)
        assertEquals("wave", keywordMatch.matchedKeyword)
        assertEquals("Paiement Wave accepté chez Dakar Roasters !", keywordMatch.replyText)

        // 3. Exact FAQ rule rejects substring and falls back to AI, accepts exact match
        val nonExact = AutomationEngine.evaluate("avez vous une promo ?", workspace, rules)
        assertEquals(AutomationMatchStage.AI_FALLBACK, nonExact.stage)

        val exactMatch = AutomationEngine.evaluate("promo", workspace, rules)
        assertEquals(AutomationMatchStage.FAQ, exactMatch.stage)
        assertEquals("Code PROMO10 actif", exactMatch.replyText)
    }

    @Test
    fun recordSearchFilter_prioritizesNameMatchesAndAppliesFilters() {
        val products = listOf(
            Product(id = "1", name = "Café Touba", category = "Boissons", price = 2500.0, stock = 20, active = true),
            Product(id = "2", name = "Coffret Cadeau", description = "Contient du café", category = "Coffrets", price = 15000.0, stock = 3, active = true),
            Product(id = "3", name = "Biscuit Café", category = "Pâtisserie", price = 1500.0, stock = 2, active = false)
        )
        // Searching "café" puts "Café Touba" (prefix) ahead of "Biscuit Café" (contains in name) ahead of "Coffret Cadeau" (description)
        val byName = RecordSearchFilter.filterProducts(products, query = "café")
        assertEquals(listOf("1", "3", "2"), byName.map { it.id })

        // Low stock + active only filters
        val lowStockActive = RecordSearchFilter.filterProducts(products, query = "", onlyActive = true, lowStockOnly = true)
        assertEquals(listOf("2"), lowStockActive.map { it.id })

        val customers = listOf(
            Customer(id = "c1", name = "Aminata Diallo", segment = "vip", totalSpent = 45000.0),
            Customer(id = "c2", name = "Moussa Ndiaye", company = "Aminata Trading", segment = "wholesale", totalSpent = 95000.0),
            Customer(id = "c3", name = "Fatou Aminata", segment = "regular", totalSpent = 12000.0)
        )
        // Prefix match "Aminata Diallo" comes first, then name-contains "Fatou Aminata", then company-match "Moussa Ndiaye"
        val customerByName = RecordSearchFilter.filterCustomers(customers, query = "aminata")
        assertEquals(listOf("c1", "c3", "c2"), customerByName.map { it.id })

        // Segment + spent sort
        val vipOnly = RecordSearchFilter.filterCustomers(customers, query = "", segmentFilter = "vip", sortBy = CustomerSortOption.SPENT_DESC)
        assertEquals(listOf("c1"), vipOnly.map { it.id })
    }

    @Test
    fun appStrings_staysWithinDalvikRegisterLimit() {
        val classes = listOf(
            AppStrings::class.java,
            ShellStrings::class.java,
            AuthStrings::class.java,
            DashboardStrings::class.java,
            CatalogStrings::class.java,
            OrdersStrings::class.java,
            CustomerStrings::class.java,
            InboxStrings::class.java,
            AutomationStrings::class.java,
            AiStrings::class.java,
            CompanyStrings::class.java,
            SearchFilterStrings::class.java,
            CommonStrings::class.java
        )
        classes.forEach { clazz ->
            clazz.constructors.forEach { ctor ->
                assertTrue(
                    "Constructor for ${clazz.simpleName} has ${ctor.parameterCount} params (must stay well below 255)",
                    ctor.parameterCount <= 100
                )
            }
        }
        assertEquals("Automatisations", FrenchStrings.autoTitle)
        assertEquals("Automation Flows", EnglishStrings.autoTitle)
    }
}

