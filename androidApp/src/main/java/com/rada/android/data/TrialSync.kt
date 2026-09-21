package com.rada.android.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val SYNC_TIMEOUT_MS = 8_000L

suspend fun syncTrial(trial: TrialPreferences) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        trial.markSynced()
        return
    }
    try {
        withTimeoutOrNull(SYNC_TIMEOUT_MS) {
            val doc = FirebaseFirestore.getInstance().collection("users").document(uid)
            var startedAt = doc.get().await().getTimestamp("trialStartedAt")
            if (startedAt == null) {
                doc.set(mapOf("trialStartedAt" to FieldValue.serverTimestamp())).await()
                startedAt = doc.get().await().getTimestamp("trialStartedAt")
            }
            trial.adoptAccountTrial(uid, startedAt?.toDate()?.time)
        }
    } catch (_: Exception) {
    } finally {
        trial.markSynced()
    }
}
