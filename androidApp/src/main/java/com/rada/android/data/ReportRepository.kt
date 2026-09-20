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

/**
 * Firestore-backed: a report submitted by one user needs to reach every other user's Dashboard
 * AND Drive screen — that's the whole point of crowd-sourced reporting. Two read methods here
 * serve two different purposes: [observeRecentReports] is a history log for Dashboard (all
 * reports, however old); [observeActiveCameraReports] is the live set Drive's SpeedAlertEngine
 * actually checks against (recent-only — an alcoblow report from 3 hours ago is likely gone).
 *
 * NEEDS VERIFICATION: this reads/writes an open "reports" collection with no auth or App Check
 * enforcement configured on the Firestore security-rules side — fine while FIREBASE_ENABLED is
 * false in LoginViewModel (there's no real signed-in user to check against yet), but tighten
 * the rules to require authentication once that flag flips, or anyone can write arbitrary
 * reports into your database.
 */
/** One submitted report, stripped to what charts need. */
data class ReportEvent(
    val type: ReportType,
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double
)

interface ReportRepository {
    /** Returns the new report's id so it can be undone with [deleteReport]. */
    suspend fun submitReport(
        type: ReportType,
        latitude: Double,
        longitude: Double,
        bearing: Float,
        placeName: String?
    ): Result<String>

    /** Removes a report the user just submitted (the Undo action). */
    suspend fun deleteReport(id: String): Result<Unit>

    /** History log for Dashboard — all reports, most recent first, no age filtering. */
    fun observeRecentReports(limit: Long = 5): Flow<List<MockData.RecentAlert>>

    /** Live set for Drive's engine — only reports within [maxAgeMinutes], as real SpeedCamera
     *  domain objects the shared SpeedAlertEngine/CameraGridIndex can use directly. */
    fun observeActiveCameraReports(maxAgeMinutes: Long = 60): Flow<List<SpeedCamera>>

    /**
     * Reports from the last [maxAgeDays] days (newest first, capped at [limit]) for charts.
     * When [centerLat]/[centerLng] are given, only reports within [radiusKm] of that point.
     */
    fun observeReportEvents(
        maxAgeDays: Long = 7,
        centerLat: Double? = null,
        centerLng: Double? = null,
        radiusKm: Double = 25.0,
        limit: Long = 300
    ): Flow<List<ReportEvent>>

    /** History log filtered to roughly [radiusKm] of (centerLat, centerLng) — an alcoblow
     *  report in Mombasa isn't relevant to someone in Embakasi, and showing it anyway just
     *  trains people to ignore the feed. */
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

    // Firestore queues writes locally and syncs when back online, but its Task only completes
    // once the server acknowledges — awaiting it unbounded would hang the UI while offline.
    // Failing fast (e.g. permission denied) surfaces as an error; a timeout means "queued".
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
                    // Prefers the geocoded name stored at submission time; falls back to raw
                    // coordinates for documents submitted before that field existed, or if
                    // geocoding failed for that particular report.
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
            // Capped at 100 docs server-side; the actual age filter is applied client-side below
            // since Firestore would otherwise need a composite index for a range query combined
            // with the existing orderBy.
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
                        // Crowd reports don't carry a known speed limit — SpeedAlertEngine already
                        // treats speedLimit <= 0 as "no limit" for the UI (see DriveViewModel's
                        // speedLimitKmh mapping), and checkpoint/alcoblow types trigger on
                        // presence alone regardless of limit anyway.
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
        // Same approach as observeActiveCameraReports: newest-first capped fetch, age and
        // distance filtered client-side so no composite index is needed.
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
        // NEEDS VERIFICATION / scale note: this fetches a capped recent batch and filters by
        // distance client-side, rather than a true geo-indexed query (which would need a
        // geohash scheme — Firestore has no native radius query). Fine at this app's current
        // report volume; revisit if the "reports" collection grows into the thousands and
        // this starts pulling more documents than it needs to just to filter most of them out.
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

    // Duplicated from SpeedAlertEngine's private haversine calc rather than extracting a
    // shared utility — small enough that the duplication is cheaper than the refactor right
    // now; worth consolidating if a third caller ever needs the same math.
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