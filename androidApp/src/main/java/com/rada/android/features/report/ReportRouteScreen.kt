package com.rada.android.features.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.rada.android.data.FirestoreReportRepository
import com.rada.android.data.ReportHistoryPreferences
import com.rada.android.data.AndroidPlaceNameResolver
import com.rada.shared.location.AndroidLocationProvider

@Composable
fun ReportRouteScreen(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ReportViewModel = viewModel {
        ReportViewModel(
            FirestoreReportRepository(),
            AndroidLocationProvider(LocationServices.getFusedLocationProviderClient(context)),
            AndroidPlaceNameResolver(context),
            ReportHistoryPreferences(context)
        )
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(hasLocationPermission) {
        viewModel.onIntent(ReportIntent.LocationPermissionChanged(hasLocationPermission))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ReportEffect.NavigateToDrive -> onDone()
                is ReportEffect.RequestLocationPermission -> onRequestPermission()
            }
        }
    }

    ReportContentScreen(state = state, onIntent = viewModel::onIntent)
}
