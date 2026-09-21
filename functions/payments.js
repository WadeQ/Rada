const { planFor } = require("./plans");

const DAY_MS = 24 * 60 * 60 * 1000;

function callbackFields(stk) {
  const items = (stk.CallbackMetadata && stk.CallbackMetadata.Item) || [];
  const get = (name) => (items.find((i) => i.Name === name) || {}).Value;
  return { amount: get("Amount"), receipt: get("MpesaReceiptNumber") };
}

const FRIENDLY = {
  1: "Your M-Pesa balance is too low for this payment.",
  1025: "We couldn't send the M-Pesa prompt. Try again.",
  1032: "You cancelled the payment.",
  1037: "We couldn't reach your phone for the M-Pesa prompt. Check the number and your network, then try again.",
  2001: "Wrong M-Pesa PIN. Try again.",
};

function friendlyMessage(resultCode) {
  return FRIENDLY[Number(resultCode)] || "The payment didn't go through. Try again.";
}

function failureStatus(resultCode) {
  return Number(resultCode) === 1032 ? "cancelled" : "failed";
}

// Applies a Daraja result (callback or STK query) to a pending payment exactly once.
async function applyResult(db, FieldValue, checkoutRequestId, { resultCode, resultDesc, amount, receipt }) {
  const ref = db.collection("payments").doc(checkoutRequestId);
  return db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    if (!snap.exists) return { applied: false, reason: "unknown" };
    const payment = snap.data();
    if (payment.status !== "pending") return { applied: false, reason: "already-final" };

    if (Number(resultCode) !== 0) {
      tx.update(ref, {
        status: failureStatus(resultCode),
        resultCode: Number(resultCode),
        resultDesc: friendlyMessage(resultCode),
        completedAt: FieldValue.serverTimestamp(),
      });
      return { applied: true, status: failureStatus(resultCode) };
    }

    if (amount !== undefined && Number(amount) !== payment.amount) {
      tx.update(ref, {
        status: "failed",
        resultCode: -1,
        resultDesc: "Amount mismatch",
        completedAt: FieldValue.serverTimestamp(),
      });
      return { applied: true, status: "failed" };
    }

    const plan = planFor(payment.planId);
    const userRef = db.collection("users").doc(payment.uid);
    const userSnap = await tx.get(userRef);
    const current = userSnap.exists && userSnap.data().proUntil ? userSnap.data().proUntil.toMillis() : 0;
    const proUntil = new Date(Math.max(Date.now(), current) + plan.days * DAY_MS);
    tx.set(userRef, { proUntil }, { merge: true });
    tx.update(ref, {
      status: "success",
      receipt: receipt || null,
      resultCode: 0,
      resultDesc: resultDesc || null,
      completedAt: FieldValue.serverTimestamp(),
    });
    return { applied: true, status: "success", proUntil };
  });
}

module.exports = { applyResult, callbackFields, friendlyMessage };
