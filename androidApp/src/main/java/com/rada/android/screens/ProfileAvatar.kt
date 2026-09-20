package com.rada.android.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.rada.android.theme.RadaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatar(
    name: String,
    photoPath: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    initialsCount: Int = 1
) {
    val photo = rememberProfilePhoto(photoPath)
    Box(
        modifier.size(size).clip(CircleShape).background(RadaColors.Accent),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            val initials = name.trim().take(initialsCount).uppercase().ifEmpty { "?" }
            Text(
                initials,
                color = RadaColors.AccentInk,
                fontSize = (size.value * if (initialsCount > 1) 0.35f else 0.4f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun rememberProfilePhoto(path: String?): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, path) {
        value = path?.let { p ->
            withContext(Dispatchers.IO) { BitmapFactory.decodeFile(p)?.asImageBitmap() }
        }
    }
    return bitmap
}
