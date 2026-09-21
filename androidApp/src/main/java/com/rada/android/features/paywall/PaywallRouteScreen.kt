package com.rada.android.features.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rada.android.data.FirebaseAuthRepository
import com.rada.android.data.PlayBillingRepository
import com.rada.android.data.ProRepository

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PaywallRouteScreen(onTrialStarted: () -> Unit) {
    val context = LocalContext.current
    val viewModel: PaywallViewModel = viewModel {
        PaywallViewModel(FirebaseAuthRepository(), MpesaPaymentRepository(), ProRepository(), PlayBillingRepository(context))
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PaywallEffect.NavigateToDashboard -> onTrialStarted()
                is PaywallEffect.ShowError -> Unit
            }
        }
    }

    PaywallContentScreen(
        state = state,
        onIntent = { intent ->
            if (intent is PaywallIntent.PlaySubscribeTapped) {
                context.findActivity()?.let(viewModel::subscribeWithPlay)
            } else {
                viewModel.onIntent(intent)
            }
        }
    )
}
