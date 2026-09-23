package com.rada.android.features.drive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.rada.android.data.TrialState
import com.rada.android.tracking.TrackingService

@Composable
fun DriveRouteScreen(
    isPro: Boolean,
    trial: TrialState,
    onStartTrial: () -> Unit,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onHome: () -> Unit,
    onReport: () -> Unit,
    onHelp: () -> Unit,
    onUnlockTapped: () -> Unit,
    onSettings: () -> Unit
) {
    val hasAccess = isPro || trial is TrialState.Active || trial is TrialState.NotStarted
    LaunchedEffect(trial, isPro) {
        if (trial is TrialState.NotStarted && !isPro) onStartTrial()
    }
    when {
        trial is TrialState.Loading -> Box(Modifier.fillMaxSize())
        !hasAccess -> DriveLockedScreen(onUpgrade = onUnlockTapped)
        else -> ActiveDriveRoute(
            isPro = isPro || trial is TrialState.Active || trial is TrialState.NotStarted,
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = onRequestPermission,
            onHome = onHome, onReport = onReport, onHelp = onHelp,
            onUnlockTapped = onUnlockTapped, onSettings = onSettings
        )
    }
}

@Composable
private fun ActiveDriveRoute(
    isPro: Boolean,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onHome: () -> Unit,
    onReport: () -> Unit,
    onHelp: () -> Unit,
    onUnlockTapped: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DriveViewModel = viewModel { DriveViewModel() }
    val state by viewModel.state.collectAsState()

    // TrackingService is the actual engine now (see its class doc) — this just makes sure it's
    // running. Deliberately NOT stopped when this screen leaves composition (switching tabs, or
    // the whole app going to the background while the driver checks WhatsApp): a paying driver
    // expects Drive to keep watching until they explicitly end it, from the notification's Stop
    // action or by signing out. The service resolves its own access/preferences/foreground
    // state independently, so nothing here needs to feed it anything beyond starting it.
    val appContext = context.applicationContext
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) TrackingService.start(appContext)
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    BatteryOptimisationPrompt(enabled = hasLocationPermission)

    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    DriveContentScreen(
        state = state,
        isPro = isPro,
        hasLocationPermission = hasLocationPermission,
        onRequestPermission = onRequestPermission,
        onHome = onHome,
        onReport = onReport,
        onHelp = onHelp,
        onUnlockTapped = onUnlockTapped,
        onSettings = onSettings
    )
}
