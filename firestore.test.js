const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_user_01";
const ADMIN_EMAIL = "sitoleluthando749@gmail.com";
const CUSTOMER_UID = "customer_alice";
const OTHER_UID = "customer_bob";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validBotPayload(overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    name: "PRAISE SCALPER PRO V6+ ENGINE",
    subtitle: "POWERED BY EA MOBILE CONNECT.",
    description: "Automated precision scalping Expert Advisor built for modern traders.",
    originalPriceZar: 1500,
    promoPriceZar: 200,
    whatsappDisplay: "069 224 7990",
    whatsappE164: "27692247990",
    buttonLabel: "BUY PRAISE SCALPER PRO",
    accentTheme: "EMERALD",
    presetImageKey: "praise_scalper",
    imageUrl: "",
    localImageName: "",
    active: true,
    sortOrder: 0,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validAnnouncementPayload(overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    title: "BLACK FRIDAY MEGA SALE LIVE",
    message: "Both flagship trading bots available at R200 until further notice.",
    badgeLabel: "BLACK FRIDAY",
    active: true,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validOfferPayload(overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    title: "BLACK FRIDAY SPECIAL",
    botName: "PRAISE SCALPER PRO V6+ ENGINE",
    originalPriceZar: 1500,
    promoPriceZar: 200,
    tagline: "PREMIUM TRADING TECHNOLOGY. UNBELIEVABLE VALUE.",
    active: true,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validSettingsPayload(uid = ADMIN_UID, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    recommendedBrokerUrl: "https://crm.razormarkets.co.za/links/go/14",
    recommendedBrokerHeading: "TRADE WITH OUR RECOMMENDED BROKER.",
    recommendedBrokerDescription: "Discover our recommended brokerage partner and explore its trading services.",
    blackFridayHeading: "BLACK FRIDAY MEGA SALE",
    blackFridaySlogan: "PREMIUM TRADING TECHNOLOGY. UNBELIEVABLE VALUE.",
    blackFridayNotice: "BLACK FRIDAY SPECIAL AVAILABLE UNTIL FURTHER NOTICE.",
    eaConnectHeading: "POWERED BY EA MOBILE CONNECT.",
    eaConnectDescription: "Our trading solutions are powered by EA MOBILE CONNECT, bringing automated trading technology and connectivity to our trading products.",
    aboutHeading: "WHO WE ARE",
    aboutDescription: "PRAISETRADERS is an algorithmic trading technology brand dedicated to providing modern automated trading solutions.",
    riskDisclaimer: "Trading financial instruments involves substantial risk. Automated trading systems do not guarantee profits.",
    updatedBy: uid,
    createdAt: now,
    updatedAt: now,
    ...overrides,
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

test("Unauthenticated user: denied read/write on all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("trading_bots").where("active", "==", true).get());
  await assertFails(unauthDb.collection("announcements").where("active", "==", true).get());
  await assertFails(unauthDb.collection("promo_offers").where("active", "==", true).get());
  await assertFails(unauthDb.collection("site_settings").doc("global").get());
  await assertFails(unauthDb.collection("trading_bots").doc("bot_1").set(validBotPayload()));
});

test("Email spoofing: unverified admin email cannot write or list inactive bots", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(spoofDb.collection("trading_bots").doc("bot_1").set(validBotPayload()));
  await assertFails(spoofDb.collection("trading_bots").get());
});

test("Bootstrapped verified admin: can CRUD bots, announcements, offers, and site_settings", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(adminDb.collection("trading_bots").doc("praise_scalper").set(validBotPayload()));
  await assertSucceeds(adminDb.collection("trading_bots").doc("praise_scalper").update({
    promoPriceZar: 250,
    active: false,
    updatedAt: new Date(Date.now() - 500),
  }));
  await assertSucceeds(adminDb.collection("trading_bots").get());

  // Reject ghost field / @DocumentId 'id' leakage
  await assertFails(adminDb.collection("trading_bots").doc("bad_bot").set(validBotPayload({ id: "bad_bot" })));
  // Reject immutable createdAt modification
  await assertFails(adminDb.collection("trading_bots").doc("praise_scalper").update({
    createdAt: new Date(Date.now() - 100),
    updatedAt: new Date(Date.now() - 100),
  }));

  await assertSucceeds(adminDb.collection("announcements").doc("ann_1").set(validAnnouncementPayload()));
  await assertSucceeds(adminDb.collection("promo_offers").doc("offer_1").set(validOfferPayload()));
  await assertSucceeds(adminDb.collection("site_settings").doc("global").set(validSettingsPayload(ADMIN_UID)));
  await assertSucceeds(adminDb.collection("site_settings").doc("global").update({
    recommendedBrokerUrl: "https://crm.razormarkets.co.za/links/go/14",
    updatedAt: new Date(Date.now() - 500),
  }));
});

test("Customer user: can query only active bots/announcements/offers and get global settings, never mutate catalog", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("trading_bots").doc("active_bot").set(validBotPayload({ active: true }));
    await db.collection("trading_bots").doc("hidden_bot").set(validBotPayload({ active: false }));
    await db.collection("announcements").doc("ann_active").set(validAnnouncementPayload({ active: true }));
    await db.collection("announcements").doc("ann_hidden").set(validAnnouncementPayload({ active: false }));
    await db.collection("promo_offers").doc("offer_active").set(validOfferPayload({ active: true }));
    await db.collection("site_settings").doc("global").set(validSettingsPayload(ADMIN_UID));
  });

  const customerDb = testEnv.authenticatedContext(CUSTOMER_UID, {
    email: "trader@example.com",
    email_verified: true,
  }).firestore();

  // Filtered queries match allow list rule (active == true)
  await assertSucceeds(customerDb.collection("trading_bots").where("active", "==", true).get());
  await assertSucceeds(customerDb.collection("announcements").where("active", "==", true).get());
  await assertSucceeds(customerDb.collection("promo_offers").where("active", "==", true).get());
  await assertSucceeds(customerDb.collection("site_settings").doc("global").get());

  // Unfiltered queries & inactive docs denied for non-admin
  await assertFails(customerDb.collection("trading_bots").get());
  await assertFails(customerDb.collection("trading_bots").doc("hidden_bot").get());
  await assertFails(customerDb.collection("site_settings").get());

  // Writes denied for non-admin
  await assertFails(customerDb.collection("trading_bots").doc("active_bot").update({
    promoPriceZar: 1,
    updatedAt: new Date(Date.now() - 500),
  }));
  await assertFails(customerDb.collection("admins").doc(CUSTOMER_UID).set({
    email: "trader@example.com",
    role: "ADMIN",
    createdAt: new Date(Date.now() - 500),
  }));
});

test("User inquiries: strict owner isolation under /users/{userId}/inquiries", async () => {
  const aliceDb = testEnv.authenticatedContext(CUSTOMER_UID).firestore();
  const bobDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(CUSTOMER_UID).collection("inquiries").doc("inq_1").set({
      userId: CUSTOMER_UID,
      botName: "PRAISE SCALPER PRO V6+ ENGINE",
      whatsappNumber: "069 224 7990",
      channel: "WHATSAPP_BUY",
      priceZar: 200,
      createdAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(CUSTOMER_UID).collection("inquiries").get());

  // Cross-user read/write denied
  await assertFails(bobDb.collection("users").doc(CUSTOMER_UID).collection("inquiries").get());
  await assertFails(
    bobDb.collection("users").doc(CUSTOMER_UID).collection("inquiries").doc("inq_2").set({
      userId: OTHER_UID,
      botName: "Hijack",
      whatsappNumber: "069 224 7990",
      channel: "WHATSAPP_BUY",
      priceZar: 200,
      createdAt: now,
    })
  );
});
