package com.rada.android.features.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rada.android.data.FirebaseAuthRepository

@Composable
fun PaywallRouteScreen(onTrialStarted: () -> Unit) {
    val viewModel: PaywallViewModel = viewModel {
        PaywallViewModel(FirebaseAuthRepository(), StubPaymentRepository())
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PaywallEffect.NavigateToDashboard -> onTrialStarted()
                is PaywallEffect.ShowError -> Unit // TODO: same SnackbarHost note as Login/Otp
            }
        }
    }

    PaywallContentScreen(state = state, onIntent = viewModel::onIntent)
}