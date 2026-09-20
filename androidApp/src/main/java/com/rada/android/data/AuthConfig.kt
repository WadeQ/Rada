package com.rada.android.data

/**
 * Single on/off switch for whether real Firebase phone-auth is wired in yet. False bypasses
 * Firebase entirely: LoginViewModel skips the real OTP round trip, and SplashRouteScreen's
 * "is this a returning user" check falls back to "has a name been saved" (UserProfilePreferences)
 * instead of asking a real FirebaseAuth session that can never exist while this is off — without
 * that fallback, every app restart would incorrectly send even a fully signed-up user back to
 * Login, since a real FirebaseUser is never created under the stub.
 *
 * Flip to true once Firebase console setup (phone provider, SHA-1, Play Integrity) is done —
 * see LoginViewModel for the OTP path this also re-enables. M-Pesa/STK push (PaymentRepository)
 * is a separate flag entirely — this only covers auth, not payments.
 */
object AuthConfig {
    const val FIREBASE_ENABLED = false
}