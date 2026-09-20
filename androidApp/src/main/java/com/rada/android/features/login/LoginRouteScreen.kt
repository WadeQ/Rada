package com.rada.android.features.login

import android.app.Activity
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
fun LoginRouteScreen(
    onNavigateToOtp: (verificationId: String, e164Phone: String) -> Unit,
    onSignedInDirectly: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel {
        LoginViewModel(FirebaseAuthRepository(), UserProfilePreferences(context))
    }
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp -> onNavigateToOtp(
                    effect.verificationId,
                    effect.e164Phone
                )

                is LoginEffect.SignedInDirectly -> onSignedInDirectly()
                is LoginEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
                is LoginEffect.ShowError -> Unit
            }
        }
    }

    Scaffold { padding ->
        LoginContentScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onSubmitTapped = { activity?.let { viewModel.submit(it) } },
            modifier = Modifier.padding(padding)
        )
    }
}
