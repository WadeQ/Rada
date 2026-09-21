package com.rada.android.features.signup

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
fun SignUpRouteScreen(
    onSignedIn: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onHaveAccount: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel: SignUpViewModel = viewModel {
        SignUpViewModel(FirebaseAuthRepository(), UserProfilePreferences(context), TrialPreferences(context))
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
                is SignUpEffect.SignedIn -> onSignedIn()
                is SignUpEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
            }
        }
    }

    Scaffold { padding ->
        SignUpContentScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onSubmit = viewModel::signUp,
            onGoogle = { activity?.let { viewModel.signUpWithGoogle(it) } },
            onApple = { activity?.let { viewModel.signUpWithApple(it) } },
            onHaveAccount = onHaveAccount,
            modifier = Modifier.padding(padding)
        )
    }
}
