const test = require("node:test");
const assert = require("node:assert");
const { accountHash, playExpiryMillis } = require("../entitlement");
const { applyPlaySubscription } = require("../playSubscriptions");

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

test("verified Play subscription grants Pro", async () => {
  const db = playDb({});
  const r = await applyPlaySubscription(db, FVD, { uid: "u1", purchaseToken: "tok", sub: sub("SUBSCRIPTION_STATE_ACTIVE", 30) });
  assert.strictEqual(r.applied, true);
  assert.ok(db.store.users.u1.proUntil.getTime() > Date.now() + 25 * 864e5);
  assert.ok(db.store.users.u1.playProUntil.getTime() > Date.now());
  assert.strictEqual(db.store.playPurchases.tok.uid, "u1");
});

test("renewing while already Pro extends from the subscription's own new expiry", async () => {
  const db = playDb({ u1: { playProUntil: new Date(Date.now() + 5 * 864e5), proUntil: new Date(Date.now() + 5 * 864e5) } });
  await applyPlaySubscription(db, FVD, { uid: "u1", purchaseToken: "tok", sub: sub("SUBSCRIPTION_STATE_ACTIVE", 30) });
  assert.ok(db.store.users.u1.proUntil.getTime() > Date.now() + 25 * 864e5);
});

test("an expired Play subscription clears Pro", async () => {
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
