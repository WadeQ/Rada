// Runs against the Firestore + Functions emulators: posts the same callbacks Safaricom would send.
const assert = require("node:assert");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, Timestamp } = require("firebase-admin/firestore");

initializeApp({ projectId: "rada-app-78472" });
const db = getFirestore();
const URL = "http://127.0.0.1:5001/rada-app-78472/europe-west1/mpesaCallback";
const TOKEN = "local-test-token";
const DAY = 864e5;

const body = (id, code, amount, desc = "The service request is processed successfully.") => ({
  Body: {
    stkCallback: {
      MerchantRequestID: "m1",
      CheckoutRequestID: id,
      ResultCode: code,
      ResultDesc: desc,
      ...(code === 0 && {
        CallbackMetadata: { Item: [{ Name: "Amount", Value: amount }, { Name: "MpesaReceiptNumber", Value: "TESTRCPT1" }, { Name: "PhoneNumber", Value: 254708374149 }] },
      }),
    },
  },
});
const post = (b, token = TOKEN) =>
  fetch(`${URL}?token=${token}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(b) });
const seed = (id, uid, planId, amount) =>
  db.collection("payments").doc(id).set({ uid, planId, amount, status: "pending", phone: "25470****149", createdAt: Timestamp.now() });
const pay = async (id) => (await db.collection("payments").doc(id).get()).data();
const user = async (uid) => (await db.collection("users").doc(uid).get()).data() || {};

(async () => {
  // 1. successful monthly payment grants 30 days of Pro
  await seed("ws_ok", "u1", "monthly", 200);
  let res = await post(body("ws_ok", 0, 200));
  assert.strictEqual(res.status, 200);
  assert.strictEqual((await pay("ws_ok")).status, "success");
  assert.strictEqual((await pay("ws_ok")).receipt, "TESTRCPT1");
  const until = (await user("u1")).proUntil.toMillis();
  assert.ok(Math.abs(until - (Date.now() + 30 * DAY)) < 60_000, "30 days of Pro");
  console.log("ok  success grants 30 days");

  // 2. Safaricom retries the same callback: nothing changes
  await post(body("ws_ok", 0, 200));
  assert.strictEqual((await user("u1")).proUntil.toMillis(), until);
  console.log("ok  duplicate callback is ignored");

  // 3. renewing stacks on the remaining time
  await seed("ws_renew", "u1", "annual", 1800);
  await post(body("ws_renew", 0, 1800));
  assert.ok(Math.abs((await user("u1")).proUntil.toMillis() - (until + 365 * DAY)) < 60_000);
  console.log("ok  renewal stacks");

  // 4. wrong amount is rejected
  await seed("ws_amt", "u2", "annual", 1800);
  await post(body("ws_amt", 0, 1));
  assert.strictEqual((await pay("ws_amt")).status, "failed");
  assert.strictEqual((await user("u2")).proUntil, undefined);
  console.log("ok  wrong amount grants nothing");

  // 5. cancelled by the user
  await seed("ws_cancel", "u3", "monthly", 200);
  await post(body("ws_cancel", 1032, 0, "Request Cancelled by user."));
  assert.strictEqual((await pay("ws_cancel")).status, "cancelled");
  assert.strictEqual((await user("u3")).proUntil, undefined);
  console.log("ok  cancel grants nothing");

  // 6. wrong token and unknown payment
  await seed("ws_tok", "u4", "monthly", 200);
  res = await post(body("ws_tok", 0, 200), "wrong");
  assert.strictEqual(res.status, 403);
  assert.strictEqual((await pay("ws_tok")).status, "pending");
  res = await post(body("ws_nope", 0, 200));
  assert.strictEqual(res.status, 200);
  assert.strictEqual((await user("u4")).proUntil, undefined);
  console.log("ok  bad token rejected, unknown payment ignored");
})().catch((e) => { console.error("FAIL", e); process.exit(1); });
