const assert = require("node:assert");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { deleteUserData } = require("../../accountDeletion");

(async () => {
  initializeApp({ projectId: "rada-deletion-test" });
  const db = getFirestore();
  await db.doc("users/alice").set({ trialStartedAt: new Date(), proUntil: new Date() });
  await db.doc("users/alice/monthly/2026-09").set({ warnings: 3, evaded: 2, savedKes: 0 });
  await db.doc("reports/r_alice1").set({ uid: "alice", type: "CAMERA" });
  await db.doc("reports/r_alice2").set({ uid: "alice", type: "CHECKPOINT" });
  await db.doc("playPurchases/tok_alice").set({ uid: "alice" });
  await db.doc("users/bob").set({ trialStartedAt: new Date() });
  await db.doc("reports/r_bob").set({ uid: "bob", type: "CAMERA" });
  await db.doc("reports/r_old").set({ type: "CAMERA" });

  const removed = await deleteUserData(db, "alice");
  assert.deepStrictEqual(removed, { reports: 2, purchases: 1 });
  for (const path of ["users/alice", "users/alice/monthly/2026-09", "reports/r_alice1", "reports/r_alice2", "playPurchases/tok_alice"]) {
    assert.strictEqual((await db.doc(path).get()).exists, false, `${path} should be gone`);
  }
  for (const path of ["users/bob", "reports/r_bob", "reports/r_old"]) {
    assert.strictEqual((await db.doc(path).get()).exists, true, `${path} should survive`);
  }
  console.log("ok  account deletion: removes the user's doc, stats, reports and purchases; leaves others alone");
})().catch((e) => { console.error("FAIL", e); process.exit(1); });
