package com.rada.shared.domain

import kotlin.math.floor

/**
 * Coarse spatial index so the alert engine never scans the full camera database on a
 * location tick. Buckets cameras into ~1km grid cells keyed by truncated lat/lon.
 *
 * Deliberately simple (no external geo library, no geohash) — this is fine for a city-scale
 * dataset. Swap for a geohash or R-tree if the camera count grows into the tens of thousands
 * or the app expands beyond Nairobi's latitude band, where longitude-cell distortion grows.
 */
class CameraGridIndex(cameras: List<SpeedCamera>) {

    companion object {
        // ~0.01 degrees latitude is roughly 1.1km — tune against real camera density.
        private const val CELL_SIZE_DEGREES = 0.01
    }

    private val grid: Map<Pair<Int, Int>, List<SpeedCamera>> =
        cameras.groupBy { cellKey(it.latitude, it.longitude) }

    private fun cellKey(lat: Double, lon: Double): Pair<Int, Int> =
        Pair(floor(lat / CELL_SIZE_DEGREES).toInt(), floor(lon / CELL_SIZE_DEGREES).toInt())

    /**
     * Cameras in the driver's current cell plus all 8 neighbors. The neighbor buffer matters
     * at cell boundaries — a camera just across a cell edge shouldn't vanish because the
     * driver's coordinate happens to round into the next cell.
     */
    fun nearby(driverLat: Double, driverLon: Double): List<SpeedCamera> {
        val (cellX, cellY) = cellKey(driverLat, driverLon)
        val result = mutableListOf<SpeedCamera>()
        for (dx in -1..1) {
            for (dy in -1..1) {
                grid[Pair(cellX + dx, cellY + dy)]?.let { result.addAll(it) }
            }
        }
        return result
    }
}
