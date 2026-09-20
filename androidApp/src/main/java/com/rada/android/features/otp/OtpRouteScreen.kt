package com.rada.android.features.otp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rada.android.data.FirebaseAuthRepository
import com.rada.android.data.UserProfilePreferences

@Composable
fun OtpRouteScreen(
    verificationId: String,
    phoneDisplay: String,
    onVerified: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: OtpViewModel = viewModel(key = verificationId) {
        OtpViewModel(
            FirebaseAuthRepository(),
            UserProfilePreferences(context),
            verificationId,
            phoneDisplay
        )
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OtpEffect.NavigateToDashboard -> onVerified()
                is OtpEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
                is OtpEffect.ShowError -> Unit // TODO: same SnackbarHost note as LoginRouteScreen
            }
        }
    }

    Scaffold { padding ->
        OtpContentScreen(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(padding)
        )
    }
}