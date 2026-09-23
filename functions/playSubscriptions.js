const { playExpiryMillis, PLAY_PRODUCTS } = require("./entitlement");

const PACKAGE_NAME = "com.rada.android";

function isOurs(sub) {
  return (sub.lineItems || []).some((item) => item.productId in PLAY_PRODUCTS);
}

async function applyPlaySubscription(db, FieldValue, { uid, purchaseToken, sub }) {
  const expiry = playExpiryMillis(sub);
  const purchaseRef = db.collection("playPurchases").doc(purchaseToken);
  const userRef = db.collection("users").doc(uid);
  return db.runTransaction(async (tx) => {
    const purchaseSnap = await tx.get(purchaseRef);
    if (purchaseSnap.exists && purchaseSnap.data().uid !== uid) return { applied: false, reason: "owned-by-another-account" };

    const item = (sub.lineItems || []).find((i) => i.productId in PLAY_PRODUCTS) || {};
    tx.set(purchaseRef, {
      uid,
      productId: item.productId || null,
      state: sub.subscriptionState,
      expiresAt: expiry ? new Date(expiry) : null,
      orderId: sub.latestOrderId || null,
      updatedAt: FieldValue.serverTimestamp(),
    });

    const proUntil = expiry ? new Date(expiry) : null;
    tx.set(userRef, {
      playProUntil: proUntil || FieldValue.delete(),
      proUntil: proUntil || FieldValue.delete(),
    }, { merge: true });
    return { applied: true, active: expiry > Date.now(), proUntil };
  });
}

function playClient() {
  const { androidpublisher } = require("@googleapis/androidpublisher");
  const { GoogleAuth } = require("google-auth-library");
  const auth = new GoogleAuth({ scopes: ["https://www.googleapis.com/auth/androidpublisher"] });
  return androidpublisher({ version: "v3", auth });
}

async function fetchSubscription(purchaseToken, client = playClient()) {
  const res = await client.purchases.subscriptionsv2.get({ packageName: PACKAGE_NAME, token: purchaseToken });
  return res.data;
}

async function acknowledgeIfNeeded(sub, purchaseToken, client = playClient()) {
  const pending = sub.acknowledgementState === "ACKNOWLEDGEMENT_STATE_PENDING";
  const item = (sub.lineItems || []).find((i) => i.productId in PLAY_PRODUCTS);
  if (!pending || !item || sub.subscriptionState !== "SUBSCRIPTION_STATE_ACTIVE") return false;
  await client.purchases.subscriptions.acknowledge({
    packageName: PACKAGE_NAME,
    subscriptionId: item.productId,
    token: purchaseToken,
    requestBody: {},
  });
  return true;
}

module.exports = { applyPlaySubscription, fetchSubscription, acknowledgeIfNeeded, isOurs, PACKAGE_NAME };
