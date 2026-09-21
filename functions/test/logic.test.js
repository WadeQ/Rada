const test = require("node:test");
const assert = require("node:assert");
const { normalizeKenyanPhone, maskPhone } = require("../phone");
const { timestamp, password } = require("../daraja");
const { planFor } = require("../plans");
const { applyResult, callbackFields } = require("../payments");

test("phone normalisation", () => {
  for (const v of ["0719731685", "719731685", "+254719731685", "254719731685", "0719 731 685"]) {
    assert.strictEqual(normalizeKenyanPhone(v), "254719731685");
  }
  assert.strictEqual(normalizeKenyanPhone("0112345678"), "254112345678");
  for (const v of ["", "12345", "0619731685", null, 254719731685]) assert.strictEqual(normalizeKenyanPhone(v), null);
  assert.strictEqual(maskPhone("254719731685"), "25471****685");
});

test("plans are server-defined", () => {
  assert.strictEqual(planFor("monthly").amountKes, 150);
  assert.strictEqual(planFor("annual").amountKes, 1200);
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
  const db = fakeDb({ status: "pending", uid: "u1", planId: "monthly", amount: 150 }, {});
  const r = await applyResult(db, FV, "p1", { resultCode: 0, amount: 150, receipt: "ABC" });
  assert.strictEqual(r.status, "success");
  assert.ok(Math.abs(db.store.users.u1.proUntil.getTime() - (Date.now() + 30 * 864e5)) < 5000);
  const again = await applyResult(db, FV, "p1", { resultCode: 0, amount: 150 });
  assert.strictEqual(again.applied, false);
});

test("renewal stacks on remaining time", async () => {
  const future = Date.now() + 10 * 864e5;
  const db = fakeDb({ status: "pending", uid: "u1", planId: "monthly", amount: 150 }, { proUntil: ts(future) });
  await applyResult(db, FV, "p1", { resultCode: 0, amount: 150 });
  assert.ok(Math.abs(db.store.users.u1.proUntil.getTime() - (future + 30 * 864e5)) < 1000);
});

test("wrong amount, cancel and unknown payment never grant Pro", async () => {
  let db = fakeDb({ status: "pending", uid: "u1", planId: "annual", amount: 1200 }, {});
  assert.strictEqual((await applyResult(db, FV, "p1", { resultCode: 0, amount: 1 })).status, "failed");
  assert.strictEqual(db.store.users.u1.proUntil, undefined);
  db = fakeDb({ status: "pending", uid: "u1", planId: "annual", amount: 1200 }, {});
  assert.strictEqual((await applyResult(db, FV, "p1", { resultCode: 1032 })).status, "cancelled");
  assert.strictEqual((await applyResult(db, FV, "nope", { resultCode: 0, amount: 1200 })).applied, false);
});

test("callback metadata parsing", () => {
  const stk = { CallbackMetadata: { Item: [{ Name: "Amount", Value: 150 }, { Name: "MpesaReceiptNumber", Value: "XYZ" }] } };
  assert.deepStrictEqual(callbackFields(stk), { amount: 150, receipt: "XYZ" });
});
