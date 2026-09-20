package com.rada.android.features.drive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.rada.android.data.FirestoreReportRepository
import com.rada.shared.location.AndroidLocationProvider

@Composable
fun DriveRouteScreen(
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
            FirestoreReportRepository()
        )
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) viewModel.onIntent(DriveIntent.StartTracking)
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
