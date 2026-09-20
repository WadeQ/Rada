package com.rada.android.features.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.rada.android.data.AuthConfig
import com.rada.android.data.FirebaseAuthRepository
import com.rada.android.data.OnboardingPreferences
import com.rada.android.data.UserProfilePreferences
import com.rada.android.screens.SplashScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The routing decision requested: first-time users (onboarding not yet completed) see
 * onboarding; returning users skip straight to login (or dashboard if already signed in AND
 * already have a saved name — a signed-in user with no name, e.g. an edge case where the app
 * was reinstalled but a Firebase session somehow persisted, still needs CompleteProfile).
 * Onboarding is a one-time gate, not a per-launch screen; so is the name prompt.
 *
 * The "is this a returning user" check itself is gated by AuthConfig.FIREBASE_ENABLED: while
 * Firebase auth is stubbed off, authRepository.isSignedIn() checks a real FirebaseUser that
 * never gets created, so it's permanently false — without the fallback below, EVERY restart
 * would incorrectly send even a fully signed-up user back to Login. A saved name is a reliable
 * stand-in for "already completed sign-up" during this stub-testing phase; once FIREBASE_ENABLED
 * flips true, this goes back to asking the real signed-in state, as it should.
 *
 * The decision fires from SplashScreen's own onFinished callback (after its ~1.8s delay),
 * not in a parallel LaunchedEffect — the earlier draft of this file ran the two side by side,
 * which could navigate away before the splash animation had actually finished showing.
 */
@Composable
fun SplashRouteScreen(
    onGoToOnboarding: () -> Unit,
    onGoToLogin: () -> Unit,
    onGoToDashboard: () -> Unit,
    onGoToCompleteProfile: () -> Unit
) {
    val context = LocalContext.current
    val onboardingPrefs = remember { OnboardingPreferences(context) }
    val userProfilePrefs = remember { UserProfilePreferences(context) }
    val authRepository = remember { FirebaseAuthRepository() }
    val scope = rememberCoroutineScope()

    SplashScreen(onFinished = {
        scope.launch {
            val hasOnboarded = onboardingPrefs.hasCompletedOnboarding.first()
            if (!hasOnboarded) {
                onGoToOnboarding()
                return@launch
            }

            val name = userProfilePrefs.userName.first()
            val isReturningUser = if (AuthConfig.FIREBASE_ENABLED) {
                authRepository.isSignedIn()
            } else {
                name != null
            }

            when {
                !isReturningUser -> onGoToLogin()
                name.isNullOrBlank() -> onGoToCompleteProfile()
                else -> onGoToDashboard()
            }
        }
    })
}