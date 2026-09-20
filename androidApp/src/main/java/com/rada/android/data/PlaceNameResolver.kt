package com.rada.android.data

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

interface PlaceNameResolver {
    /** Null on any failure — caller falls back to showing raw coordinates rather than "null". */
    suspend fun resolve(latitude: Double, longitude: Double): String?
}

class AndroidPlaceNameResolver(private val context: Context) : PlaceNameResolver {
    override suspend fun resolve(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(latitude, longitude, 1)
                val address = results?.firstOrNull() ?: return@withContext null
                listOfNotNull(address.thoroughfare, address.subLocality ?: address.locality)
                    .joinToString(", ")
                    .ifBlank { null }
            } catch (e: Exception) {
                null
            }
        }
}