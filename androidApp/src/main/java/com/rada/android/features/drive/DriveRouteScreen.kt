package com.rada.android.features.drive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.rada.android.data.AlertPreferences
import com.rada.android.data.TrialState
import com.rada.android.data.FirestoreReportRepository
import com.rada.android.data.StatsRepository
import com.rada.android.tracking.AlertNotifications
import com.rada.android.tracking.AlertSpeaker
import com.rada.android.tracking.TrackingService
import com.rada.shared.location.AndroidLocationProvider

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
    val viewModel: DriveViewModel = viewModel {
        DriveViewModel(
            AndroidLocationProvider(LocationServices.getFusedLocationProviderClient(context)),
            FirestoreReportRepository(),
            AlertPreferences(context),
            AlertNotifications(context.applicationContext),
            AlertSpeaker(context),
            StatsRepository()
        )
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(isPro) { viewModel.onIntent(DriveIntent.ProChanged(isPro)) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) viewModel.onIntent(DriveIntent.StartTracking)
    }

    val appContext = context.applicationContext
    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) TrackingService.start(appContext)
        onDispose { TrackingService.stop(appContext) }
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onIntent(DriveIntent.AppResumed)
                Lifecycle.Event.ON_STOP -> viewModel.onIntent(DriveIntent.AppPaused)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
