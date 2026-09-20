package com.rada.android.features.drive

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun buildCarIcon(density: Float): BitmapDescriptor {
    val w = (26 * density).toInt()
    val h = (46 * density).toInt()
    val bmp = createBitmap(w, h)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val d = density

    p.color = 0x55000000
    c.drawRoundRect(RectF(2 * d, 3 * d, w - 1 * d, h - 0.5f * d), 9 * d, 9 * d, p)

    p.color = 0xFFFFFFFF.toInt()
    c.drawRoundRect(RectF(1 * d, 1 * d, w - 2 * d, h - 3 * d), 9 * d, 9 * d, p)
    p.color = 0xFF7A1F3D.toInt()
    c.drawRoundRect(RectF(3 * d, 3 * d, w - 4 * d, h - 5 * d), 7 * d, 7 * d, p)

    p.color = 0xFF1A0A10.toInt()
    c.drawRoundRect(RectF(6 * d, 13 * d, w - 7 * d, 22 * d), 3 * d, 3 * d, p)
    c.drawRoundRect(RectF(6.5f * d, 28 * d, w - 7.5f * d, 34 * d), 2.5f * d, 2.5f * d, p)

    p.color = 0xFFF2A93B.toInt()
    c.drawCircle(7.5f * d, 5.5f * d, 1.8f * d, p)
    c.drawCircle(w - 8.5f * d, 5.5f * d, 1.8f * d, p)

    return BitmapDescriptorFactory.fromBitmap(bmp)
}
