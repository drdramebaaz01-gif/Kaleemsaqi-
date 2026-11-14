const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read products or orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").get());
  await assertFails(unauthDb.collection("orders").get());
});

test("Customer: cannot create product (admin only)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  await assertFails(
    aliceDb.collection("products").doc("prod_1").set({
      name: "T-Shirt",
      code: "TSHIRT-001",
      price: 1500,
      category: "Men's Fashion",
      stock: 10,
      createdAt: new Date(),
    })
  );
});

test("Admin: can create and update products", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "kaleemsaqi01@gmail.com" }).firestore();
  await assertSucceeds(
    adminDb.collection("products").doc("prod_1").set({
      name: "Men Casual Shirt",
      code: "SHIRT-001",
      price: 2499,
      discountPercentage: 15,
      category: "Men's Fashion",
      stock: 50,
      createdAt: new Date(),
    })
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  await assertSucceeds(aliceDb.collection("products").doc("prod_1").get());
});

test("Customer: can create own order, other customer cannot read it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_1").set({
      userId: ALICE_UID,
      customerName: "Alice Smith",
      mobileNumber: "03001234567",
      city: "Lahore",
      deliveryAddress: "Street 10, Model Town",
      totalAmount: 2499,
      paymentMethod: "Cash on Delivery",
      status: "Pending",
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com" }).firestore();
  await assertFails(bobDb.collection("orders").doc("order_1").get());

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "kaleemsaqi01@gmail.com" }).firestore();
  await assertSucceeds(adminDb.collection("orders").doc("order_1").get());
});

test("Customer: can manage own cart items", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_1").set({
      productId: "prod_1",
      name: "Men Casual Shirt",
      price: 2499,
      quantity: 2,
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com" }).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_1").get()
  );
});
