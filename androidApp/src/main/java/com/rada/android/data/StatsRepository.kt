package com.rada.android.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.rada.shared.domain.FineEstimates
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MonthStats(val warnings: Int = 0, val evaded: Int = 0, val savedKes: Int = 0)

class StatsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun monthKey() = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    fun recordWarning() = record(warnings = 1, evaded = 0, savedKes = 0)

    fun recordEvaded() = record(warnings = 0, evaded = 1, savedKes = FineEstimates.AVG_SPEED_FINE_KES)

    private fun record(warnings: Int, evaded: Int, savedKes: Int) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("monthly").document(monthKey())
            .set(
                mapOf(
                    "warnings" to FieldValue.increment(warnings.toLong()),
                    "evaded" to FieldValue.increment(evaded.toLong()),
                    "savedKes" to FieldValue.increment(savedKes.toLong()),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }

    val monthStats: Flow<MonthStats> = callbackFlow {
        var docListener: ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            docListener?.remove()
            docListener = null
            val uid = firebaseAuth.currentUser?.uid
            if (uid == null) {
                trySend(MonthStats())
            } else {
                docListener = firestore.collection("users").document(uid).collection("monthly").document(monthKey())
                    .addSnapshotListener { snapshot, _ ->
                        trySend(
                            MonthStats(
                                warnings = snapshot?.getLong("warnings")?.toInt() ?: 0,
                                evaded = snapshot?.getLong("evaded")?.toInt() ?: 0,
                                savedKes = snapshot?.getLong("savedKes")?.toInt() ?: 0
                            )
                        )
                    }
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            docListener?.remove()
        }
    }
}
