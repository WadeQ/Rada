package com.rada.android.data

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthWebException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

sealed interface SignInResult {
    data class Success(val isNewUser: Boolean = false) : SignInResult
    data object Cancelled : SignInResult
    data class Failed(val message: String) : SignInResult
}

interface AuthRepository {
    suspend fun signUpWithUsername(username: String, password: String): SignInResult
    suspend fun signInWithUsername(username: String, password: String): SignInResult
    suspend fun signInWithGoogle(activity: Activity): SignInResult
    suspend fun signInWithGoogleOneTap(activity: Activity, authorizedOnly: Boolean): SignInResult
    suspend fun signInWithApple(activity: Activity): SignInResult
    fun isSignedIn(): Boolean
    fun currentUserDisplayName(): String?
    suspend fun reloadUser()
    suspend fun updateDisplayName(name: String)
    fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("europe-west1")
) : AuthRepository {

    override suspend fun signUpWithUsername(username: String, password: String): SignInResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(usernameToEmail(username), password).await()
            result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(username.trim()).build())?.await()
            SignInResult.Success(isNewUser = true)
        } catch (e: Exception) {
            SignInResult.Failed(friendlyAuthError(e, signingUp = true))
        }
    }

    override suspend fun signInWithUsername(username: String, password: String): SignInResult {
        return try {
            auth.signInWithEmailAndPassword(usernameToEmail(username), password).await()
            SignInResult.Success()
        } catch (e: Exception) {
            SignInResult.Failed(friendlyAuthError(e, signingUp = false))
        }
    }

    override suspend fun signInWithGoogle(activity: Activity): SignInResult {
        val webClientId = webClientId(activity)
            ?: return SignInResult.Failed("Google sign-in isn't set up yet. Add the web client ID (see README).")
        return requestGoogle(activity, GetSignInWithGoogleOption.Builder(webClientId).build(), surfaceErrors = true)
    }

    override suspend fun signInWithGoogleOneTap(activity: Activity, authorizedOnly: Boolean): SignInResult {
        val webClientId = webClientId(activity) ?: return SignInResult.Cancelled
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(authorizedOnly)
            .setAutoSelectEnabled(false)
            .build()
        return requestGoogle(activity, option, surfaceErrors = false)
    }

    private suspend fun requestGoogle(activity: Activity, option: CredentialOption, surfaceErrors: Boolean): SignInResult {
        return try {
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val result = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
                SignInResult.Success(result.additionalUserInfo?.isNewUser == true)
            } else if (surfaceErrors) {
                SignInResult.Failed("Google sign-in returned an unexpected result.")
            } else {
                SignInResult.Cancelled
            }
        } catch (e: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (e: NoCredentialException) {
            if (surfaceErrors) SignInResult.Failed("No Google account found on this phone. Add one in Settings and try again.")
            else SignInResult.Cancelled
        } catch (e: Exception) {
            if (surfaceErrors) SignInResult.Failed(friendlyAuthError(e, signingUp = false)) else SignInResult.Cancelled
        }
    }

    override suspend fun signInWithApple(activity: Activity): SignInResult {
        return try {
            val provider = OAuthProvider.newBuilder("apple.com").setScopes(listOf("email", "name")).build()
            val pending = auth.pendingAuthResult
            val result = if (pending != null) pending.await() else auth.startActivityForSignInWithProvider(activity, provider).await()
            SignInResult.Success(result.additionalUserInfo?.isNewUser == true)
        } catch (e: FirebaseAuthWebException) {
            if (e.message?.contains("cancel", ignoreCase = true) == true) SignInResult.Cancelled
            else SignInResult.Failed(appleFailureMessage(e))
        } catch (e: Exception) {
            SignInResult.Failed(appleFailureMessage(e))
        }
    }

    private fun appleFailureMessage(e: Exception): String {
        val message = e.message.orEmpty()
        return if (
            message.contains("internal error", ignoreCase = true) ||
            message.contains("not allowed", ignoreCase = true) ||
            message.contains("certificate hash", ignoreCase = true)
        ) {
            "Apple sign-in isn't turned on yet. Please use a username and password for now."
        } else {
            message.ifBlank { "Apple sign-in failed" }
        }
    }

    private fun webClientId(activity: Activity): String? {
        val res = activity.resources
        listOf("default_web_client_id", "google_web_client_id").forEach { name ->
            val id = res.getIdentifier(name, "string", activity.packageName)
            if (id != 0) res.getString(id).takeIf { it.isNotBlank() }?.let { return it }
        }
        return null
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override fun currentUserDisplayName(): String? = auth.currentUser?.displayName

    override suspend fun reloadUser() {
        auth.currentUser?.reload()?.await()
    }

    override suspend fun updateDisplayName(name: String) {
        runCatching {
            auth.currentUser?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
        }
    }

    override fun signOut() {
        auth.signOut()
        TrialPreferences.resetSync()
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        functions.getHttpsCallable("deleteAccount").call().await()
        signOut()
    }

    private fun usernameToEmail(username: String) = "${username.trim().lowercase()}@users.rada.invalid"
}

internal fun friendlyAuthError(e: Exception, signingUp: Boolean): String {
    val code = (e as? FirebaseAuthException)?.errorCode.orEmpty()
    val message = e.message.orEmpty()
    return when {
        e is FirebaseTooManyRequestsException || code == "ERROR_TOO_MANY_REQUESTS" ->
            "Too many attempts. Please wait a while and try again."
        e is FirebaseNetworkException || message.contains("network", ignoreCase = true) || message.contains("unreachable host", ignoreCase = true) ->
            "No internet connection. Check your connection and try again."
        code == "ERROR_EMAIL_ALREADY_IN_USE" -> "That username is taken. Try another one."
        code == "ERROR_WEAK_PASSWORD" -> "Choose a stronger password (at least 8 characters)."
        code == "ERROR_OPERATION_NOT_ALLOWED" || message.contains("not allowed", ignoreCase = true) ->
            "Username sign-in isn't switched on yet. Please try again soon."
        !signingUp && (code in setOf("ERROR_INVALID_CREDENTIAL", "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_EMAIL")
            || message.contains("credential", ignoreCase = true) || message.contains("no user record", ignoreCase = true)) ->
            "Wrong username or password."
        else -> message.ifBlank { "Something went wrong. Please try again." }
    }
}
