package com.rada.shared.location

import LocationProvider
import com.rada.shared.domain.DriverState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBestForNavigation
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosLocationProvider(
    private val locationManager: CLLocationManager = CLLocationManager()
) : LocationProvider {

    override fun observeLocation(): Flow<DriverState> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                val speedKmh =
                    maxOf(loc.speed, 0.0) * 3.6
                val coordinate = loc.coordinate
                trySend(
                    DriverState(
                        latitude = coordinate.useContents { latitude },
                        longitude = coordinate.useContents { longitude },
                        speedCurrent = speedKmh,
                        bearingCurrent = loc.course.toFloat()
                    )
                )
            }
        }
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.startUpdatingLocation()
        awaitClose { locationManager.stopUpdatingLocation() }
    }

    override suspend fun getLastKnownLocation(): DriverState? {
        val loc = locationManager.location ?: return null
        val speedKmh = maxOf(loc.speed, 0.0) * 3.6
        val coordinate = loc.coordinate
        return DriverState(
            latitude = coordinate.useContents { latitude },
            longitude = coordinate.useContents { longitude },
            speedCurrent = speedKmh,
            bearingCurrent = loc.course.toFloat()
        )
    }
}
