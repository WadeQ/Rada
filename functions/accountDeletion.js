async function deleteUserData(db, uid) {
  const writer = db.bulkWriter();
  const reports = await db.collection("reports").where("uid", "==", uid).get();
  reports.forEach((doc) => writer.delete(doc.ref));
  const purchases = await db.collection("playPurchases").where("uid", "==", uid).get();
  purchases.forEach((doc) => writer.delete(doc.ref));
  await writer.close();
  await db.recursiveDelete(db.collection("users").doc(uid));
  return { reports: reports.size, purchases: purchases.size };
}

module.exports = { deleteUserData };
