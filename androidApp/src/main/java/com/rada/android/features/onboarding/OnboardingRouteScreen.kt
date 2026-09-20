package com.rada.android.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.rada.android.data.OnboardingPreferences
import com.rada.android.screens.OnboardingScreen
import kotlinx.coroutines.launch

@Composable
fun OnboardingRouteScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val onboardingPrefs = remember { OnboardingPreferences(context) }
    val scope = rememberCoroutineScope()

    OnboardingScreen(onDone = {
        scope.launch {
            onboardingPrefs.setOnboardingComplete()
            onDone()
        }
    })
}
