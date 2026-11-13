const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_workspace_123";
const BOB_UID = "bob_workspace_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validWorkspace(uid) {
  const now = new Date();
  return {
    ownerId: uid,
    companyName: "Boutique Teranga",
    industry: "Boutique & Commerce",
    currency: "XOF",
    planTier: "growth",
    whatsappStatus: "not_connected",
    welcomeEnabled: true,
    welcomeMessage: "Bonjour ! Bienvenue chez Boutique Teranga.",
    aiEnabled: true,
    aiTone: "friendly",
    aiLanguage: "Français",
    aiInstructions: "Répondez poliment avec Wave et Orange Money.",
    acceptedPaymentMethods: ["wave", "orange_money", "mtn_momo", "moov_money", "cash_on_delivery"],
    mobileMoneyNumber: "+221 77 123 45 67",
    email: "bonjour@teranga.sn",
    phone: "+221 77 123 45 67",
    seededDemoData: true,
    createdAt: now,
    updatedAt: now,
  };
}

function validCustomer(uid) {
  const now = new Date();
  return {
    ownerId: uid,
    name: "Aminata Diallo",
    phone: "+221 77 234 56 78",
    email: "aminata@example.com",
    segment: "vip",
    tags: ["wave", "dakar"],
    notes: "Livraison Plateau",
    totalOrders: 4,
    totalSpent: 48500,
    createdAt: now,
    updatedAt: now,
  };
}

function validProduct(uid) {
  const now = new Date();
  return {
    ownerId: uid,
    name: "Café Touba Moulu 500g",
    description: "Torréfaction artisanale aux épices",
    category: "Épicerie",
    sku: "COF-001",
    price: 3500,
    stock: 25,
    active: true,
    imageKey: "coffee",
    localImagePath: "/data/user/0/com.example/files/product_images/prod_1.jpg",
    createdAt: now,
    updatedAt: now,
  };
}

function validOrder(uid, status = "pending") {
  const now = new Date();
  return {
    ownerId: uid,
    orderNumber: "WP-1042",
    customerId: "cust_1",
    customerName: "Aminata Diallo",
    customerPhone: "+221 77 234 56 78",
    items: [
      { productId: "prod_1", productName: "Café Touba Moulu 500g", quantity: 2, unitPrice: 3500 },
    ],
    totalAmount: 7000,
    status,
    paymentMethod: "wave",
    paymentStatus: "paid",
    paymentReference: "WV-99821",
    notes: "Livrer avant 18h",
    channel: "sandbox",
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write workspaces or customers", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("workspaces").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("workspaces").doc(ALICE_UID).collection("customers").get());
  await assertFails(unauthDb.collection("workspaces").doc(ALICE_UID).set(validWorkspace(ALICE_UID)));
});

test("Multi-tenant isolation: Bob cannot read or write Alice workspace or subcollections", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("workspaces").doc(ALICE_UID).set(validWorkspace(ALICE_UID));
    await db.collection("workspaces").doc(ALICE_UID).collection("customers").doc("c1").set(validCustomer(ALICE_UID));
    await db.collection("workspaces").doc(ALICE_UID).collection("products").doc("p1").set(validProduct(ALICE_UID));
    await db.collection("workspaces").doc(ALICE_UID).collection("orders").doc("o1").set(validOrder(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).collection("customers").get());
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).collection("customers").doc("c1").get());
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).collection("products").get());
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).collection("orders").get());
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).collection("customers").doc("c2").set(validCustomer(BOB_UID)));
});

test("Owner Alice can create and update her own workspace, customers, products, and orders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const wsRef = aliceDb.collection("workspaces").doc(ALICE_UID);
  await assertSucceeds(wsRef.set(validWorkspace(ALICE_UID)));
  await assertSucceeds(wsRef.get());
  await assertSucceeds(wsRef.collection("customers").doc("c1").set(validCustomer(ALICE_UID)));
  await assertSucceeds(wsRef.collection("customers").get());
  await assertSucceeds(wsRef.collection("products").doc("p1").set(validProduct(ALICE_UID)));
  await assertSucceeds(wsRef.collection("orders").doc("o1").set(validOrder(ALICE_UID)));
});

test("Shadow update / ghost field and identity spoofing are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const wsRef = aliceDb.collection("workspaces").doc(ALICE_UID);

  // Identity spoofing
  await assertFails(wsRef.set(validWorkspace(BOB_UID)));

  // Ghost field injection
  const withGhost = { ...validCustomer(ALICE_UID), isAdmin: true };
  await assertFails(wsRef.collection("customers").doc("c1").set(withGhost));

  // Malicious document ID poisoning
  await assertFails(wsRef.collection("customers").doc("bad$id!").set(validCustomer(ALICE_UID)));
});

test("Cancelled orders are locked against further updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("workspaces").doc(ALICE_UID).collection("orders").doc("o1");
  await assertSucceeds(orderRef.set(validOrder(ALICE_UID, "pending")));
  await assertSucceeds(orderRef.update({ status: "cancelled", updatedAt: new Date() }));
  await assertFails(orderRef.update({ status: "confirmed", updatedAt: new Date() }));
});
