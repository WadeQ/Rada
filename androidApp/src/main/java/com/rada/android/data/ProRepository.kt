package com.rada.android.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val proUntilMillis: Flow<Long?> = callbackFlow {
        var docListener: ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            docListener?.remove()
            docListener = null
            val uid = firebaseAuth.currentUser?.uid
            if (uid == null) {
                trySend(null)
            } else {
                docListener = firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, _ ->
                        trySend(snapshot?.getTimestamp("proUntil")?.toDate()?.time)
                    }
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            docListener?.remove()
        }
    }

    suspend fun fetchProUntilMillis(): Long? {
        val uid = auth.currentUser?.uid ?: return null
        return firestore.collection("users").document(uid).get(Source.SERVER).await()
            .getTimestamp("proUntil")?.toDate()?.time
    }
}
