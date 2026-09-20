package com.rada.shared.domain

import kotlin.math.floor

class CameraGridIndex(cameras: List<SpeedCamera>) {

    companion object {
        private const val CELL_SIZE_DEGREES = 0.01
    }

    private val grid: Map<Pair<Int, Int>, List<SpeedCamera>> =
        cameras.groupBy { cellKey(it.latitude, it.longitude) }

    private fun cellKey(lat: Double, lon: Double): Pair<Int, Int> =
        Pair(floor(lat / CELL_SIZE_DEGREES).toInt(), floor(lon / CELL_SIZE_DEGREES).toInt())

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
