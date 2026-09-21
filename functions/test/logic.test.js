const test = require("node:test");
const assert = require("node:assert");
const { normalizeKenyanPhone, maskPhone } = require("../phone");
const { timestamp, password } = require("../daraja");
const { planFor } = require("../plans");
const { applyResult, callbackFields, friendlyMessage } = require("../payments");
const { accountHash, playExpiryMillis, currentExpiries, combinedProUntil } = require("../entitlement");
const { applyPlaySubscription } = require("../playSubscriptions");

test("phone normalisation", () => {
  for (const v of ["0719731685", "719731685", "+254719731685", "254719731685", "0719 731 685"]) {
    assert.strictEqual(normalizeKenyanPhone(v), "254719731685");
  }
  assert.strictEqual(normalizeKenyanPhone("0112345678"), "254112345678");
  for (const v of ["", "12345", "0619731685", null, 254719731685]) assert.strictEqual(normalizeKenyanPhone(v), null);
  assert.strictEqual(maskPhone("254719731685"), "25471****685");
});

test("plans are server-defined", () => {
  assert.strictEqual(planFor("monthly").amountKes, 200);
  assert.strictEqual(planFor("annual").amountKes, 1800);
  assert.strictEqual(planFor("__proto__"), null);
  assert.strictEqual(planFor("free"), null);
});

test("daraja timestamp is Nairobi time and password is base64", () => {
  assert.strictEqual(timestamp(new Date("2026-01-02T23:30:05Z")), "20260103023005");
  assert.strictEqual(password("174379", "key", "20260103023005"), Buffer.from("174379key20260103023005").toString("base64"));
});

function fakeDb(payment, user) {
  const store = { payments: { p1: payment }, users: { u1: user } };
  const ref = (col, id) => ({ col, id });
  return {
    store,
    collection: (col) => ({ doc: (id) => ref(col, id) }),
    runTransaction: async (fn) =>
      fn({
        get: async ({ col, id }) => ({ exists: !!store[col][id], data: () => store[col][id] }),
        update: ({ col, id }, d) => Object.assign(store[col][id], d),
        set: ({ col, id }, d) => (store[col][id] = { ...(store[col][id] || {}), ...d }),
      }),
  };
}
const FV = { serverTimestamp: () => "ts" };
const ts = (ms) => ({ toMillis: () => ms });

test("success extends Pro and is applied once", async () => {
  const db = fakeDb({ status: "pending", uid: "u1", planId: "monthly", amount: 200 }, {});
  const r = await applyResult(db, FV, "p1", { resultCode: 0, amount: 200, receipt: "ABC" });
  assert.strictEqual(r.status, "success");
  assert.ok(Math.abs(db.store.users.u1.proUntil.getTime() - (Date.now() + 30 * 864e5)) < 5000);
  const again = await applyResult(db, FV, "p1", { resultCode: 0, amount: 200 });
  assert.strictEqual(again.applied, false);
});

test("renewal stacks on remaining time", async () => {
  const future = Date.now() + 10 * 864e5;
  const db = fakeDb({ status: "pending", uid: "u1", planId: "monthly", amount: 200 }, { proUntil: ts(future) });
  await applyResult(db, FV, "p1", { resultCode: 0, amount: 200 });
  assert.ok(Math.abs(db.store.users.u1.proUntil.getTime() - (future + 30 * 864e5)) < 1000);
});

test("wrong amount, cancel and unknown payment never grant Pro", async () => {
  let db = fakeDb({ status: "pending", uid: "u1", planId: "annual", amount: 1800 }, {});
  assert.strictEqual((await applyResult(db, FV, "p1", { resultCode: 0, amount: 1 })).status, "failed");
  assert.strictEqual(db.store.users.u1.proUntil, undefined);
  db = fakeDb({ status: "pending", uid: "u1", planId: "annual", amount: 1800 }, {});
  assert.strictEqual((await applyResult(db, FV, "p1", { resultCode: 1032 })).status, "cancelled");
  assert.strictEqual((await applyResult(db, FV, "nope", { resultCode: 0, amount: 1800 })).applied, false);
});

test("callback metadata parsing", () => {
  const stk = { CallbackMetadata: { Item: [{ Name: "Amount", Value: 200 }, { Name: "MpesaReceiptNumber", Value: "XYZ" }] } };
  assert.deepStrictEqual(callbackFields(stk), { amount: 200, receipt: "XYZ" });
});

test("Daraja result codes get friendly messages", async () => {
  assert.match(friendlyMessage(1037), /couldn't reach your phone/);
  assert.match(friendlyMessage(2001), /PIN/);
  assert.match(friendlyMessage(9999), /didn't go through/);
  const db = fakeDb({ status: "pending", uid: "u1", planId: "monthly", amount: 200 }, {});
  await applyResult(db, FV, "p1", { resultCode: 1037, resultDesc: "DS timeout user cannot be reached" });
  assert.strictEqual(db.store.payments.p1.status, "failed");
  assert.match(db.store.payments.p1.resultDesc, /couldn't reach your phone/);
});

const inDays = (d) => new Date(Date.now() + d * 864e5).toISOString();
const sub = (state, days, productId = "rada_pro_monthly") => ({
  subscriptionState: state,
  latestOrderId: "GPA.1",
  lineItems: [{ productId, expiryTime: inDays(days) }],
});

test("Play states map to access", () => {
  for (const s of ["ACTIVE", "CANCELED", "IN_GRACE_PERIOD"]) {
    assert.ok(playExpiryMillis(sub(`SUBSCRIPTION_STATE_${s}`, 5)) > Date.now(), s);
  }
  for (const s of ["ON_HOLD", "PAUSED", "EXPIRED", "PENDING"]) {
    assert.strictEqual(playExpiryMillis(sub(`SUBSCRIPTION_STATE_${s}`, 5)), 0, s);
  }
  assert.strictEqual(playExpiryMillis(sub("SUBSCRIPTION_STATE_ACTIVE", 5, "some_other_product")), 0);
  assert.strictEqual(playExpiryMillis(null), 0);
});

test("account hash is stable and hides the uid", () => {
  assert.strictEqual(accountHash("abc"), accountHash("abc"));
  assert.notStrictEqual(accountHash("abc"), accountHash("abd"));
  assert.strictEqual(accountHash("abc").length, 32);
  assert.ok(!accountHash("abc").includes("abc"));
});

test("proUntil is the later of M-Pesa and Play; legacy proUntil counts as M-Pesa", () => {
  const t1 = ts(Date.now() + 5 * 864e5), t2 = ts(Date.now() + 40 * 864e5);
  assert.strictEqual(combinedProUntil({ mpesa: 0, play: 0 }), null);
  assert.strictEqual(combinedProUntil(currentExpiries({ mpesaProUntil: t1, playProUntil: t2 })).getTime(), t2.toMillis());
  assert.deepStrictEqual(currentExpiries({ proUntil: t1 }), { mpesa: t1.toMillis(), play: 0 });
});

function playDb(users = {}, purchases = {}) {
  const store = { users, playPurchases: purchases };
  const ref = (col, id) => ({ col, id });
  return {
    store,
    collection: (col) => ({ doc: (id) => ref(col, id) }),
    runTransaction: async (fn) =>
      fn({
        get: async ({ col, id }) => ({ exists: !!store[col][id], data: () => store[col][id] }),
        set: ({ col, id }, d, opts) => {
          const next = opts && opts.merge ? { ...(store[col][id] || {}), ...d } : d;
          for (const k of Object.keys(next)) if (next[k] && next[k].__delete) delete next[k];
          store[col][id] = next;
        },
      }),
  };
}
const FVD = { serverTimestamp: () => "ts", delete: () => ({ __delete: true }) };

test("verified Play subscription grants Pro and keeps the longer M-Pesa time", async () => {
  const db = playDb({ u1: { mpesaProUntil: ts(Date.now() + 90 * 864e5), proUntil: new Date(Date.now() + 90 * 864e5) } });
  const r = await applyPlaySubscription(db, FVD, { uid: "u1", purchaseToken: "tok", sub: sub("SUBSCRIPTION_STATE_ACTIVE", 30) });
  assert.strictEqual(r.applied, true);
  assert.ok(db.store.users.u1.proUntil.getTime() > Date.now() + 80 * 864e5, "later M-Pesa expiry wins");
  assert.ok(db.store.users.u1.playProUntil.getTime() > Date.now());
  assert.strictEqual(db.store.playPurchases.tok.uid, "u1");
});

test("an expired Play subscription removes Play time but not M-Pesa time", async () => {
  const db = playDb({ u1: { playProUntil: new Date(Date.now() + 5 * 864e5) } }, { tok: { uid: "u1" } });
  await applyPlaySubscription(db, FVD, { uid: "u1", purchaseToken: "tok", sub: sub("SUBSCRIPTION_STATE_EXPIRED", -1) });
  assert.strictEqual(db.store.users.u1.playProUntil, undefined);
  assert.strictEqual(db.store.users.u1.proUntil, undefined);
});

test("a purchase token can't be claimed by a second account", async () => {
  const db = playDb({}, { tok: { uid: "owner" } });
  const r = await applyPlaySubscription(db, FVD, { uid: "thief", purchaseToken: "tok", sub: sub("SUBSCRIPTION_STATE_ACTIVE", 30) });
  assert.strictEqual(r.applied, false);
  assert.strictEqual(db.store.users.thief, undefined);
});
