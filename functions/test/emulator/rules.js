const fs = require("node:fs");
const assert = require("node:assert");
const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { doc, setDoc, getDoc, updateDoc, deleteDoc, serverTimestamp } = require("firebase/firestore");

(async () => {
  const env = await initializeTestEnvironment({
    projectId: "rada-rules-test",
    firestore: { rules: fs.readFileSync("../firestore.rules", "utf8"), host: "127.0.0.1", port: 8080 },
  });
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, "payments/p_alice"), { uid: "alice", status: "pending" });
    await setDoc(doc(db, "users/alice"), { trialStartedAt: new Date() });
    await setDoc(doc(db, "reports/r_alice"), { uid: "alice", type: "CAMERA" });
    await setDoc(doc(db, "reports/r_old"), { type: "CAMERA" });
  });
  const alice = env.authenticatedContext("alice").firestore();
  const bob = env.authenticatedContext("bob").firestore();
  const anon = env.unauthenticatedContext().firestore();

  await assertFails(updateDoc(doc(alice, "users/alice"), { proUntil: new Date(Date.now() + 864e7) }));
  await assertFails(setDoc(doc(bob, "users/bob"), { trialStartedAt: serverTimestamp(), proUntil: new Date(Date.now() + 864e7) }));
  await assertSucceeds(setDoc(doc(bob, "users/bob"), { trialStartedAt: serverTimestamp() }));
  await assertFails(setDoc(doc(bob, "users/bob"), { trialStartedAt: serverTimestamp() }, { merge: true }).then(() => updateDoc(doc(bob, "users/bob"), { trialStartedAt: new Date(0) })));
  await assertFails(getDoc(doc(bob, "users/alice")));
  await assertSucceeds(getDoc(doc(alice, "users/alice")));
  console.log("ok  users: no client can grant Pro or edit the trial; reads are private");

  await assertSucceeds(getDoc(doc(alice, "payments/p_alice")));
  await assertFails(getDoc(doc(bob, "payments/p_alice")));
  await assertFails(setDoc(doc(alice, "payments/p_new"), { uid: "alice", status: "success" }));
  await assertFails(updateDoc(doc(alice, "payments/p_alice"), { status: "success" }));
  console.log("ok  payments: owner read only, no client writes");

  const good = { uid: "alice", type: "CAMERA", latitude: -1.3, longitude: 36.8, timestamp: serverTimestamp() };
  await assertSucceeds(setDoc(doc(alice, "reports/r_new"), good));
  await assertFails(setDoc(doc(alice, "reports/r_x"), { ...good, uid: "bob" }));
  await assertFails(setDoc(doc(alice, "reports/r_y"), { ...good, type: "BOMB" }));
  await assertFails(setDoc(doc(anon, "reports/r_z"), good));
  await assertFails(getDoc(doc(anon, "reports/r_alice")));
  await assertSucceeds(getDoc(doc(bob, "reports/r_alice")));
  await assertFails(deleteDoc(doc(bob, "reports/r_alice")));
  await assertSucceeds(deleteDoc(doc(alice, "reports/r_alice")));
  await assertFails(deleteDoc(doc(alice, "reports/r_old")));
  console.log("ok  reports: validated creates, owner-only delete, sign-in required");

  await env.cleanup();
})().catch((e) => { console.error("FAIL", e); process.exit(1); });
