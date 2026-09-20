package com.rada.android.features.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.rada.android.data.FirestoreReportRepository
import com.rada.android.data.UserProfilePreferences
import com.rada.shared.location.AndroidLocationProvider

@Composable
fun DashboardRouteScreen(
    isPro: Boolean,
    onDrive: () -> Unit,
    onReport: () -> Unit,
    onHelp: () -> Unit,
    onUpgrade: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    val viewModel: DashboardViewModel = viewModel {
        DashboardViewModel(
            UserProfilePreferences(context),
            FirestoreReportRepository(),
            if (hasLocationPermission) AndroidLocationProvider(
                LocationServices.getFusedLocationProviderClient(
                    context
                )
            ) else null
        )
    }
    val state by viewModel.state.collectAsState()

    DashboardContentScreen(
        state = state,
        isPro = isPro,
        onDrive = onDrive,
        onReport = onReport,
        onHelp = onHelp,
        onUpgrade = onUpgrade,
        onSettings = onSettings
    )
}