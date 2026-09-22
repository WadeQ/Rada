package com.rada.android.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.rada.shared.domain.ReportType
import kotlinx.coroutines.launch

class RadaCarHomeScreen(carContext: CarContext) : Screen(carContext) {

    init {
        lifecycleScope.launch {
            CarDriveState.state.collect { invalidate() }
        }
    }

    override fun onGetTemplate(): Template {
        val snapshot = CarDriveState.state.value
        val list = ItemList.Builder()

        if (!snapshot.trackingActive) {
            list.addItem(
                Row.Builder()
                    .setTitle("Open Rada on your phone")
                    .addText("Start Drive there to see live alerts here")
                    .build()
            )
        } else {
            snapshot.activeAlert?.let { alert -> list.addItem(alertRow(alert)) }
                ?: list.addItem(
                    Row.Builder()
                        .setTitle("No warnings right now")
                        .addText("${snapshot.nearbyCameraCount} cameras nearby")
                        .build()
                )

            list.addItem(
                Row.Builder()
                    .setTitle("${snapshot.speedKmh} km/h")
                    .addText("Current speed")
                    .build()
            )

            list.addItem(
                Row.Builder()
                    .setTitle("This drive")
                    .addText("${snapshot.evadedThisDrive} cameras evaded · ${snapshot.warningsThisDrive} warnings")
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setHeader(
                Header.Builder()
                    .setTitle("Rada")
                    .setStartHeaderAction(Action.APP_ICON)
                    .build()
            )
            .setSingleList(list.build())
            .build()
    }

    private fun alertRow(alert: CarDriveState.CarAlert): Row {
        val what = when (alert.type) {
            ReportType.CAMERA -> "Speed camera ahead"
            ReportType.CHECKPOINT -> "Police checkpoint ahead"
            ReportType.ALCOBLOW -> "Alcohol check ahead"
        }
        val limit = alert.speedLimitKmh?.takeIf { it > 0 }?.let { " · limit $it km/h" } ?: ""
        val where = alert.placeName?.let { ", $it" } ?: ""
        return Row.Builder()
            .setTitle(what)
            .addText("${alert.distanceMeters} m$where$limit")
            .build()
    }
}
