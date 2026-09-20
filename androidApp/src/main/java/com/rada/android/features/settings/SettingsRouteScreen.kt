package com.rada.android.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.rada.android.data.AlertPreferences
import com.rada.android.data.FirebaseAuthRepository
import com.rada.android.data.ProfilePhotoStore
import com.rada.android.data.ThemePreferences
import com.rada.android.data.UserProfilePreferences

@Composable
fun SettingsRouteScreen(
    onHome: () -> Unit,
    onDrive: () -> Unit,
    onReport: () -> Unit,
    onHelp: () -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(
            ThemePreferences(context),
            UserProfilePreferences(context),
            AlertPreferences(context),
            ProfilePhotoStore(context),
            FirebaseAuthRepository()
        )
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.NavigateToLogin -> onSignedOut()
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onIntent(SettingsIntent.PhotoPicked(uri))
    }

    SettingsContentScreen(
        state = state,
        onPickPhoto = {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onIntent = viewModel::onIntent,
        onHome = onHome,
        onDrive = onDrive,
        onReport = onReport,
        onHelp = onHelp
    )
}