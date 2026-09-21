const crypto = require("node:crypto");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret, defineString } = require("firebase-functions/params");
const { planFor } = require("./plans");
const { normalizeKenyanPhone, maskPhone } = require("./phone");
const { Daraja } = require("./daraja");
const { applyResult, callbackFields } = require("./payments");

initializeApp();
const db = getFirestore();

const consumerKey = defineSecret("MPESA_CONSUMER_KEY");
const consumerSecret = defineSecret("MPESA_CONSUMER_SECRET");
const passkey = defineSecret("MPESA_PASSKEY");
const callbackToken = defineSecret("MPESA_CALLBACK_TOKEN");
const mpesaEnv = defineString("MPESA_ENV", { default: "sandbox" });
const shortcode = defineString("MPESA_SHORTCODE", { default: "174379" });
const callbackBase = defineString("MPESA_CALLBACK_BASE", { default: "" });

const SECRETS = [consumerKey, consumerSecret, passkey, callbackToken];
const REGION = "europe-west1";
const MIN_RETRY_MS = 30_000;

function daraja() {
  return new Daraja({
    env: mpesaEnv.value(),
    consumerKey: consumerKey.value(),
    consumerSecret: consumerSecret.value(),
    shortcode: shortcode.value(),
    passkey: passkey.value(),
  });
}

exports.initiateMpesaPayment = onCall({ region: REGION, secrets: SECRETS }, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in to subscribe.");
  const uid = request.auth.uid;

  const plan = planFor(request.data && request.data.planId);
  if (!plan) throw new HttpsError("invalid-argument", "Unknown plan.");
  const msisdn = normalizeKenyanPhone(request.data && request.data.phone);
  if (!msisdn) throw new HttpsError("invalid-argument", "Enter a valid Safaricom number.");
  if (!callbackBase.value()) throw new HttpsError("failed-precondition", "Payments are not configured yet.");

  const pending = await db.collection("payments").where("uid", "==", uid).where("status", "==", "pending").get();
  const inFlight = pending.docs.some((d) => Date.now() - (d.data().createdAt ? d.data().createdAt.toMillis() : 0) < MIN_RETRY_MS);
  if (inFlight) throw new HttpsError("resource-exhausted", "A payment is already in progress. Check your phone.");

  const callbackUrl = `${callbackBase.value()}?token=${encodeURIComponent(callbackToken.value())}`;
  const res = await daraja().stkPush({
    msisdn,
    amount: plan.amountKes,
    accountReference: "RadaPro",
    description: plan.label.slice(0, 13),
    callbackUrl,
  });
  if (!res.ok || String(res.json.ResponseCode) !== "0") {
    console.error("STK push rejected", res.status, res.json.errorCode || res.json.ResponseCode);
    throw new HttpsError("unavailable", "Couldn't start the M-Pesa prompt. Try again.");
  }

  const checkoutId = res.json.CheckoutRequestID;
  await db.collection("payments").doc(checkoutId).set({
    uid,
    planId: request.data.planId,
    amount: plan.amountKes,
    phone: maskPhone(msisdn),
    status: "pending",
    merchantRequestId: res.json.MerchantRequestID,
    createdAt: FieldValue.serverTimestamp(),
  });
  return { checkoutRequestId: checkoutId };
});

exports.mpesaCallback = onRequest({ region: REGION, secrets: SECRETS }, async (req, res) => {
  const given = Buffer.from(String(req.query.token || ""));
  const expected = Buffer.from(callbackToken.value());
  if (req.method !== "POST" || given.length !== expected.length || !crypto.timingSafeEqual(given, expected)) {
    res.status(403).send("forbidden");
    return;
  }
  const stk = req.body && req.body.Body && req.body.Body.stkCallback;
  if (stk && stk.CheckoutRequestID) {
    try {
      await applyResult(db, FieldValue, stk.CheckoutRequestID, {
        resultCode: stk.ResultCode,
        resultDesc: stk.ResultDesc,
        ...callbackFields(stk),
      });
    } catch (e) {
      console.error("callback processing failed", e);
    }
  }
  res.json({ ResultCode: 0, ResultDesc: "Accepted" });
});

exports.queryMpesaPayment = onCall({ region: REGION, secrets: SECRETS }, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in first.");
  const id = request.data && request.data.checkoutRequestId;
  if (typeof id !== "string" || !id) throw new HttpsError("invalid-argument", "Missing payment reference.");
  const ref = db.collection("payments").doc(id);
  let snap = await ref.get();
  if (!snap.exists || snap.data().uid !== request.auth.uid) throw new HttpsError("not-found", "Payment not found.");

  const age = Date.now() - (snap.data().createdAt ? snap.data().createdAt.toMillis() : 0);
  if (snap.data().status === "pending" && age > 20_000) {
    const q = await daraja().stkQuery(id);
    if (q.ok && q.json.ResultCode !== undefined) {
      await applyResult(db, FieldValue, id, { resultCode: q.json.ResultCode, resultDesc: q.json.ResultDesc });
      snap = await ref.get();
    }
  }
  const p = snap.data();
  return { status: p.status, message: p.status === "pending" ? null : p.resultDesc || null };
});
