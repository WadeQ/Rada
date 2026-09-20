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
