const crypto = require("node:crypto");

const PLAY_PRODUCTS = { rada_pro_monthly: "monthly", rada_pro_annual: "annual" };
const ACCESS_STATES = new Set([
  "SUBSCRIPTION_STATE_ACTIVE",
  "SUBSCRIPTION_STATE_CANCELED",
  "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
]);

// The obfuscated account id the app attaches to a purchase: it ties a Play purchase to one account.
function accountHash(uid) {
  return crypto.createHash("sha256").update(uid).digest("hex").slice(0, 32);
}

// Expiry (ms) that a Play subscription resource grants, or 0 when it grants no access right now.
function playExpiryMillis(sub) {
  if (!sub || !ACCESS_STATES.has(sub.subscriptionState)) return 0;
  const times = (sub.lineItems || [])
    .filter((item) => item.productId in PLAY_PRODUCTS)
    .map((item) => Date.parse(item.expiryTime))
    .filter((t) => !Number.isNaN(t));
  return times.length ? Math.max(...times) : 0;
}

const millis = (ts) => (ts && typeof ts.toMillis === "function" ? ts.toMillis() : 0);

// Each payment route keeps its own expiry; proUntil is whichever ends later.
// Accounts that only have a legacy proUntil are treated as M-Pesa time.
function currentExpiries(user) {
  const data = user || {};
  const play = millis(data.playProUntil);
  const legacy = data.mpesaProUntil === undefined && data.playProUntil === undefined ? millis(data.proUntil) : 0;
  return { mpesa: millis(data.mpesaProUntil) || legacy, play };
}

function combinedProUntil({ mpesa, play }) {
  const latest = Math.max(mpesa, play);
  return latest > 0 ? new Date(latest) : null;
}

module.exports = { PLAY_PRODUCTS, accountHash, playExpiryMillis, currentExpiries, combinedProUntil };
