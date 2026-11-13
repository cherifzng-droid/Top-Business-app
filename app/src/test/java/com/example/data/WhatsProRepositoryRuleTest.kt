package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.AutomationRule
import com.example.model.Customer
import com.example.model.OrderLineItem
import com.example.model.Product
import com.example.model.WorkspaceProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class WhatsProRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ownerWorkspaceSeedAndCrud_succeedsWithinPrivateWorkspace() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = WhatsProRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.seedDemoWorkspaceData("Boutique Teranga", "Boutique & Commerce", "XOF", ALICE_EMAIL)
        }

        val workspace = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getWorkspace(uid) }
        assertNotNull(workspace)
        assertEquals("Boutique Teranga", workspace?.companyName)
        assertEquals("XOF", workspace?.currency)
        assertEquals("not_connected", workspace?.whatsappStatus)
        assertTrue(workspace?.acceptedPaymentMethods?.contains("wave") == true)
        assertTrue(workspace?.acceptedPaymentMethods?.contains("orange_money") == true)

        val customers = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomers(uid).first { it.isNotEmpty() }
        }
        assertTrue(customers.size >= 5)

        val newProdId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveProduct(
                Product(
                    name = "Thé Touba 500g",
                    description = "Café moulu aux épices",
                    category = "Épicerie",
                    price = 3500.0,
                    stock = 10,
                    active = true,
                    imageKey = "coffee",
                    localImagePath = "/data/user/0/com.example/files/product_images/prod_test.jpg"
                )
            )
        }
        assertTrue(newProdId.isNotEmpty())

        val orderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createOrder(
                customer = customers.first(),
                items = listOf(OrderLineItem(newProdId, "Thé Touba 500g", 2, 3500.0)),
                notes = "Livraison Dakar Plateau",
                paymentMethod = "wave",
                paymentStatus = "pending",
                paymentReference = "WV-TEST-2026"
            )
        }
        assertTrue(orderId.isNotEmpty())

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.updateOrderPaymentStatus(orderId, "paid")
        }
        val orders = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeOrders(uid).first { list -> list.any { it.id == orderId && it.paymentStatus == "paid" } }
        }
        assertTrue(orders.any { it.id == orderId && it.paymentMethod == "wave" && it.paymentStatus == "paid" })
    }

    @Test
    fun crossTenantAccess_otherBusinessUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = WhatsProRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            val existing = aliceRepo.getWorkspace(aliceUid)
            aliceRepo.createOrUpdateWorkspace(
                WorkspaceProfile(
                    companyName = "Alice Private Biz",
                    industry = "Retail Shop",
                    currency = "USD",
                    welcomeMessage = "Hello from Alice"
                ),
                isCreate = existing == null
            )
            aliceRepo.saveCustomer(Customer(name = "Private Lead", phone = "+15550001111", segment = "lead"))
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = WhatsProRepository(firestore, auth)

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getWorkspace(aliceUid) }
            fail("Expected PERMISSION_DENIED when Bob reads Alice workspace")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull(fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }

        try {
            withTimeout(FLOW_TIMEOUT_MS) { bobRepo.observeCustomers(aliceUid).first() }
            fail("Expected PERMISSION_DENIED when Bob lists Alice customers")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull(fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    @Test
    fun unauthenticatedCaller_observeAutomations_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = WhatsProRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) { repo.observeAutomations("any_workspace").first() }
            fail("Expected failure when unauthenticated")
        } catch (e: Exception) {
            // Repository requires auth or Firestore rejects unauthenticated caller
            assertTrue(e is IllegalStateException || e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }
    }

    @Test
    fun automationRule_createAndToggle_succeeds() = runBlocking {
        val uid = signInTestUser("carol.rules@whatspro-test.com")
        val repo = WhatsProRepository(firestore, auth)
        val existing = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getWorkspace(uid) }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createOrUpdateWorkspace(
                WorkspaceProfile(companyName = "Test Biz", welcomeMessage = "Hi!"),
                isCreate = existing == null
            )
        }
        val ruleId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveAutomationRule(
                AutomationRule(
                    name = "Pricing Rule",
                    triggerType = "keyword",
                    keywords = listOf("price", "cost"),
                    matchMode = "contains",
                    responseText = "Plans start at $19/mo",
                    category = "Sales",
                    active = true
                )
            )
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.toggleAutomationRule(ruleId, false)
        }
        val rules = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAutomations(uid).first { list -> list.any { it.id == ruleId && !it.active } }
        }
        assertTrue(rules.any { it.id == ruleId && !it.active })
    }

    private companion object {
        const val ALICE_EMAIL = "alice.biz@whatspro-test.com"
        const val BOB_EMAIL = "bob.biz@whatspro-test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
