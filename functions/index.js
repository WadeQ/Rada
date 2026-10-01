const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getAuth } = require("firebase-admin/auth");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onMessagePublished } = require("firebase-functions/v2/pubsub");
const { PLAY_PRODUCTS, accountHash, playExpiryMillis } = require("./entitlement");
const { applyPlaySubscription, fetchSubscription, acknowledgeIfNeeded, isOurs } = require("./playSubscriptions");
const { deleteUserData } = require("./accountDeletion");

initializeApp();
const db = getFirestore();

const REGION = "europe-west1";

exports.verifyPlaySubscription = onCall({ region: REGION }, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in first.");
  const { productId, purchaseToken } = request.data || {};
  if (!(productId in PLAY_PRODUCTS) || typeof purchaseToken !== "string" || purchaseToken.length < 10 || purchaseToken.length > 2000) {
    throw new HttpsError("invalid-argument", "That purchase can't be verified.");
  }
  const uid = request.auth.uid;

  let sub;
  try {
    sub = await fetchSubscription(purchaseToken);
  } catch (e) {
    console.error("Play lookup failed", e && e.code, e && e.message);
    throw new HttpsError("unavailable", "Couldn't confirm your purchase with Google Play. Try again shortly.");
  }
  const bound = sub.externalAccountIdentifiers && sub.externalAccountIdentifiers.obfuscatedExternalAccountId;
  if (!isOurs(sub) || bound !== accountHash(uid)) {
    throw new HttpsError("permission-denied", "This purchase belongs to a different account.");
  }
  const result = await applyPlaySubscription(db, FieldValue, { uid, purchaseToken, sub });
  if (!result.applied) throw new HttpsError("permission-denied", "This purchase belongs to a different account.");
  try {
    await acknowledgeIfNeeded(sub, purchaseToken);
  } catch (e) {
    console.error("Play acknowledge failed", e && e.message);
  }
  return { active: result.active, proUntilMillis: result.proUntil ? result.proUntil.getTime() : null };
});

exports.playNotifications = onMessagePublished({ topic: "play-rtdn", region: REGION }, async (event) => {
  const message = event.data.message.json || {};
  const note = message.subscriptionNotification;
  if (!note || !note.purchaseToken) {
    console.log("play notification ignored", Object.keys(message).join(","));
    return;
  }
  const purchase = await db.collection("playPurchases").doc(note.purchaseToken).get();
  if (!purchase.exists) {
    console.log("play notification for an unverified purchase; the app will verify it");
    return;
  }
  const sub = await fetchSubscription(note.purchaseToken);
  await applyPlaySubscription(db, FieldValue, { uid: purchase.data().uid, purchaseToken: note.purchaseToken, sub });
  await acknowledgeIfNeeded(sub, note.purchaseToken).catch((e) => console.error("Play acknowledge failed", e.message));
  console.log("play notification applied", note.notificationType, playExpiryMillis(sub) > Date.now() ? "active" : "inactive");
});

exports.deleteAccount = onCall({ region: REGION }, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in first.");
  const uid = request.auth.uid;
  try {
    const removed = await deleteUserData(db, uid);
    await getAuth().deleteUser(uid);
    console.log("account deleted", removed.reports, "reports", removed.purchases, "purchases");
    return { deleted: true };
  } catch (e) {
    console.error("account deletion failed", e && e.code, e && e.message);
    throw new HttpsError("internal", "Couldn't delete your account right now. Please try again.");
  }
});
