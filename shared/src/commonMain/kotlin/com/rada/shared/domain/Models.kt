package com.rada.shared.domain

/**
 * The kind of thing a driver can report or be warned about.
 * Kept as its own type (not a boolean) because the app now distinguishes
 * three cases with different severity and different monetization rules:
 * camera and checkpoint reports are free to submit and free to receive,
 * alcoblow reports are free to submit but Pro-gated on the receiving side.
 */
enum class ReportType {
    CAMERA,
    CHECKPOINT,
    ALCOBLOW
}

/**
 * A speed camera or NTSA checkpoint location.
 *
 * @property isMobile Distinguishes crowdsourced/temporary checkpoints from fixed cameras.
 *   Mobile entries are expected to carry a separate expiry/confidence mechanism upstream
 *   (server-side, not modeled here) — this class only holds the geometry and category.
 */
data class SpeedCamera(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedLimit: Double,
    val bearing: Float,
    val type: ReportType = ReportType.CAMERA,
    val isMobile: Boolean = false,
    /** Human-readable location, when known — e.g. "Thika Superhighway, Allsops area" for a
     *  seed camera, or a geocoded result for a crowd report. Null falls back to a generic
     *  "Speed camera ahead" style label rather than showing raw coordinates to the driver. */
    val placeName: String? = null
)

/** The driver's real-time telemetry snapshot, as delivered by a location update. */
data class DriverState(
    val latitude: Double,
    val longitude: Double,
    val speedCurrent: Double, // km/h
    val bearingCurrent: Float
)

/**
 * Per-camera alert lifecycle. Without this, the engine would re-fire the alert on every
 * location update while the driver stays inside the alert radius — e.g. slow traffic near
 * a camera would buzz the phone continuously instead of once.
 */
enum class CameraAlertStatus {
    NOT_ALERTED,
    ALERTED,
    PASSED
}

/** Tracks one camera's alert state across successive engine evaluations. In-memory only. */
data class CameraTrackingState(
    var status: CameraAlertStatus = CameraAlertStatus.NOT_ALERTED,
    var lastDistanceMeters: Double = Double.MAX_VALUE
)