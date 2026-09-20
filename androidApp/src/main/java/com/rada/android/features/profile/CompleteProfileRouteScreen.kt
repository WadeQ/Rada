package com.rada.android.features.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rada.android.data.UserProfilePreferences

@Composable
fun CompleteProfileRouteScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val viewModel: CompleteProfileViewModel = viewModel {
        CompleteProfileViewModel(UserProfilePreferences(context))
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CompleteProfileEffect.NavigateToDashboard -> onDone()
            }
        }
    }

    CompleteProfileContentScreen(state = state, onIntent = viewModel::onIntent)
}
