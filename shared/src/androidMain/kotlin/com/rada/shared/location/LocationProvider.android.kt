package com.rada.shared.location

import LocationProvider
import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.rada.shared.domain.DriverState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val UPDATE_INTERVAL_MS = 1000L
private const val MIN_UPDATE_INTERVAL_MS = 500L
private const val MPS_TO_KMH = 3.6

class AndroidLocationProvider(
    private val fusedClient: FusedLocationProviderClient
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override fun observeLocation(): Flow<DriverState> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
            .build()

        var previous: Location? = null

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    val speedMps = if (loc.hasSpeed()) loc.speed else speedFromPreviousFix(previous, loc)
                    previous = loc
                    trySend(DriverState(loc.latitude, loc.longitude, speedMps * MPS_TO_KMH, loc.bearing))
                }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    private fun speedFromPreviousFix(previous: Location?, current: Location): Float {
        if (previous == null) return 0f
        val seconds = (current.elapsedRealtimeNanos - previous.elapsedRealtimeNanos) / 1_000_000_000f
        if (seconds <= 0f) return 0f
        return previous.distanceTo(current) / seconds
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): DriverState? =
        suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    val result = location?.let {
                        DriverState(
                            it.latitude,
                            it.longitude,
                            (it.speed * 3.6),
                            it.bearing
                        )
                    }
                    continuation.resume(result)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
}
