const crypto = require("node:crypto");

const PLAY_PRODUCTS = { rada_pro_monthly: "monthly", rada_pro_annual: "annual" };
const ACCESS_STATES = new Set([
  "SUBSCRIPTION_STATE_ACTIVE",
  "SUBSCRIPTION_STATE_CANCELED",
  "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
]);

function accountHash(uid) {
  return crypto.createHash("sha256").update(uid).digest("hex").slice(0, 32);
}

function playExpiryMillis(sub) {
  if (!sub || !ACCESS_STATES.has(sub.subscriptionState)) return 0;
  const times = (sub.lineItems || [])
    .filter((item) => item.productId in PLAY_PRODUCTS)
    .map((item) => Date.parse(item.expiryTime))
    .filter((t) => !Number.isNaN(t));
  return times.length ? Math.max(...times) : 0;
}

module.exports = { PLAY_PRODUCTS, accountHash, playExpiryMillis };
