package com.rada.android.data

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

/** What the OTP flow reports back to the ViewModel at each stage. */
sealed interface OtpEvent {
    data class CodeSent(val verificationId: String) : OtpEvent
    data class AutoVerified(val credential: PhoneAuthCredential) : OtpEvent
    data class Failed(val message: String) : OtpEvent
}

sealed interface SignInResult {
    data object Success : SignInResult
    data class Failed(val message: String) : SignInResult
}

/**
 * Abstracted behind an interface so the ViewModel (and its tests, eventually) don't depend on
 * the Firebase SDK directly — swapping providers or mocking for tests only touches this file.
 */
interface AuthRepository {
    fun requestOtp(e164Phone: String, activity: Activity): Flow<OtpEvent>
    suspend fun verifyOtp(verificationId: String, code: String): SignInResult
    suspend fun signInWithCredential(credential: PhoneAuthCredential): SignInResult
    fun isSignedIn(): Boolean
    /** Null if not signed in, or if signed in via the FIREBASE_ENABLED=false stub in
     * LoginViewModel (which never creates a real FirebaseUser at all). Paywall falls back to
     * asking for the phone number manually when this is null. */
    fun currentUserPhone(): String?

    /** Safe to call regardless of AuthConfig.FIREBASE_ENABLED — signing out a null/no-op
     *  session (the stub's permanent state) is a harmless no-op on Firebase's side. */
    fun signOut()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override fun requestOtp(e164Phone: String, activity: Activity): Flow<OtpEvent> = callbackFlow {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(OtpEvent.AutoVerified(credential))
            }

            override fun onVerificationFailed(e: FirebaseException) {
                trySend(OtpEvent.Failed(e.message ?: "Verification failed"))
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                trySend(OtpEvent.CodeSent(verificationId))
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(e164Phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose { }
    }

    override suspend fun verifyOtp(verificationId: String, code: String): SignInResult {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return signInWithCredential(credential)
    }

    override suspend fun signInWithCredential(credential: PhoneAuthCredential): SignInResult {
        return try {
            auth.signInWithCredential(credential).await()
            SignInResult.Success
        } catch (e: Exception) {
            SignInResult.Failed(e.message ?: "Sign-in failed")
        }
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override fun currentUserPhone(): String? = auth.currentUser?.phoneNumber

    override fun signOut() = auth.signOut()
}