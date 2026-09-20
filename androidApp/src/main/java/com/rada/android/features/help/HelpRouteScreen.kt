package com.rada.android.features.help

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

@Composable
fun HelpRouteScreen(
    onHome: () -> Unit,
    onDrive: () -> Unit,
    onReport: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HelpViewModel = viewModel { HelpViewModel() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HelpEffect.LaunchDialer -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:${effect.phoneNumber}".toUri()
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    HelpContentScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onHome = onHome,
        onDrive = onDrive,
        onReport = onReport,
        onSettings = onSettings
    )
}
