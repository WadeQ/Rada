package com.rada.android.features.login

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rada.android.data.FirebaseAuthRepository
import com.rada.android.data.TrialPreferences
import com.rada.android.data.UserProfilePreferences

@Composable
fun LoginRouteScreen(
    onSignedInDirectly: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel: LoginViewModel = viewModel {
        LoginViewModel(FirebaseAuthRepository(), UserProfilePreferences(context), TrialPreferences(context))
    }
    val state by viewModel.state.collectAsState()

    var oneTapShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!oneTapShown && activity != null) {
            oneTapShown = true
            viewModel.tryOneTap(activity)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.SignedInDirectly -> onSignedInDirectly()
                is LoginEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
            }
        }
    }

    Scaffold { padding ->
        LoginContentScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onSignIn = viewModel::signIn,
            onGoogle = { activity?.let { viewModel.signInWithGoogle(it) } },
            onApple = { activity?.let { viewModel.signInWithApple(it) } },
            onCreateAccount = onCreateAccount,
            modifier = Modifier.padding(padding)
        )
    }
}
