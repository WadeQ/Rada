package com.rada.android.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rada.shared.data.MockData
import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class ReportEvent(
    val type: ReportType,
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double
)

interface ReportRepository {
    suspend fun submitReport(
        type: ReportType,
        latitude: Double,
        longitude: Double,
        bearing: Float,
        placeName: String?
    ): Result<String>

    suspend fun deleteReport(id: String): Result<Unit>

    fun observeRecentReports(limit: Long = 5): Flow<List<MockData.RecentAlert>>

    fun observeActiveCameraReports(maxAgeMinutes: Long = 60): Flow<List<SpeedCamera>>

    fun observeReportEvents(
        maxAgeDays: Long = 7,
        centerLat: Double? = null,
        centerLng: Double? = null,
        radiusKm: Double = 25.0,
        limit: Long = 300
    ): Flow<List<ReportEvent>>

    fun observeNearbyReports(
        centerLat: Double,
        centerLng: Double,
        radiusKm: Double,
        limit: Long = 5
    ): Flow<List<MockData.RecentAlert>>
}

private const val WRITE_ACK_TIMEOUT_MS = 2_000L

class FirestoreReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReportRepository {

    private val collection = firestore.collection("reports")

    override suspend fun submitReport(
        type: ReportType,
        latitude: Double,
        longitude: Double,
        bearing: Float,
        placeName: String?
    ): Result<String> {
        val ref = collection.document()
        val doc = hashMapOf(
            "type" to type.name,
            "latitude" to latitude,
            "longitude" to longitude,
            "bearing" to bearing,
            "placeName" to placeName,
            "timestamp" to FieldValue.serverTimestamp()
        )
        return try {
            withTimeoutOrNull(WRITE_ACK_TIMEOUT_MS) { ref.set(doc).await() }
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReport(id: String): Result<Unit> = try {
        withTimeoutOrNull(WRITE_ACK_TIMEOUT_MS) { collection.document(id).delete().await() }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observeRecentReports(limit: Long): Flow<List<MockData.RecentAlert>> =
        callbackFlow {
            val query = collection.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit)
            val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val alerts = snapshot.documents.mapNotNull { doc ->
                    val typeName = doc.getString("type") ?: return@mapNotNull null
                    val type =
                        runCatching { ReportType.valueOf(typeName) }.getOrDefault(ReportType.CAMERA)
                    val lat = doc.getDouble("latitude")
                    val lng = doc.getDouble("longitude")
                    val timestampMillis = doc.getTimestamp("timestamp")?.toDate()?.time
                    val storedPlaceName = doc.getString("placeName")
                    MockData.RecentAlert(
                        place = storedPlaceName ?: if (lat != null && lng != null) {
                            "Near ${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                        } else "Unknown location",
                        whenLabel = formatRelativeTime(timestampMillis),
                        type = type
                    )
                }
                trySend(alerts)
            }
            awaitClose { registration.remove() }
        }

    override fun observeActiveCameraReports(maxAgeMinutes: Long): Flow<List<SpeedCamera>> =
        callbackFlow {
            val query = collection.orderBy("timestamp", Query.Direction.DESCENDING).limit(100)
            val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val cutoffMillis = System.currentTimeMillis() - maxAgeMinutes * 60_000
                val cameras = snapshot.documents.mapNotNull { doc ->
                    val typeName = doc.getString("type") ?: return@mapNotNull null
                    val type =
                        runCatching { ReportType.valueOf(typeName) }.getOrDefault(ReportType.CAMERA)
                    val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                    val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                    val timestampMillis =
                        doc.getTimestamp("timestamp")?.toDate()?.time ?: return@mapNotNull null
                    if (timestampMillis < cutoffMillis) return@mapNotNull null
                    val bearing = doc.getDouble("bearing")?.toFloat() ?: 0f
                    SpeedCamera(
                        id = doc.id,
                        latitude = lat,
                        longitude = lng,
                        speedLimit = 0.0,
                        bearing = bearing,
                        type = type,
                        placeName = doc.getString("placeName"),
                        isMobile = true
                    )
                }
                trySend(cameras)
            }
            awaitClose { registration.remove() }
        }

    override fun observeReportEvents(
        maxAgeDays: Long,
        centerLat: Double?,
        centerLng: Double?,
        radiusKm: Double,
        limit: Long
    ): Flow<List<ReportEvent>> = callbackFlow {
        val query = collection.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit)
        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val cutoffMillis = System.currentTimeMillis() - maxAgeDays * 24 * 60 * 60_000
            val events = snapshot.documents.mapNotNull { doc ->
                val type = runCatching { ReportType.valueOf(doc.getString("type") ?: return@mapNotNull null) }
                    .getOrNull() ?: return@mapNotNull null
                val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                val at = doc.getTimestamp("timestamp")?.toDate()?.time ?: return@mapNotNull null
                if (at < cutoffMillis) return@mapNotNull null
                if (centerLat != null && centerLng != null &&
                    haversineKm(centerLat, centerLng, lat, lng) > radiusKm
                ) return@mapNotNull null
                ReportEvent(type, at, lat, lng)
            }
            trySend(events)
        }
        awaitClose { registration.remove() }
    }

    override fun observeNearbyReports(
        centerLat: Double,
        centerLng: Double,
        radiusKm: Double,
        limit: Long
    ): Flow<List<MockData.RecentAlert>> = callbackFlow {
        val query = collection.orderBy("timestamp", Query.Direction.DESCENDING).limit(50)
        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val alerts = snapshot.documents.mapNotNull { doc ->
                val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                if (haversineKm(centerLat, centerLng, lat, lng) > radiusKm) return@mapNotNull null

                val typeName = doc.getString("type") ?: return@mapNotNull null
                val type =
                    runCatching { ReportType.valueOf(typeName) }.getOrDefault(ReportType.CAMERA)
                val timestampMillis = doc.getTimestamp("timestamp")?.toDate()?.time
                val storedPlaceName = doc.getString("placeName")
                MockData.RecentAlert(
                    place = storedPlaceName ?: "Near ${"%.4f".format(lat)}, ${"%.4f".format(lng)}",
                    whenLabel = formatRelativeTime(timestampMillis),
                    type = type
                )
            }.take(limit.toInt())
            trySend(alerts)
        }
        awaitClose { registration.remove() }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2).let { it * it } +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2).let { it * it }
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    private fun formatRelativeTime(timestampMillis: Long?): String {
        if (timestampMillis == null) return "Just now"
        val diffMinutes = (System.currentTimeMillis() - timestampMillis) / 60000
        return when {
            diffMinutes < 1 -> "Just now"
            diffMinutes < 60 -> "$diffMinutes min ago"
            diffMinutes < 60 * 24 -> "${diffMinutes / 60} h ago"
            diffMinutes < 60 * 24 * 2 -> "Yesterday"
            else -> "${diffMinutes / (60 * 24)} days ago"
        }
    }
}
