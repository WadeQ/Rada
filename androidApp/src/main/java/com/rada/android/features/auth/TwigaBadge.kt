package com.rada.android.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rada.android.R
import com.rada.android.theme.RadaPalette

@Composable
fun TwigaBadge(modifier: Modifier = Modifier, size: Dp = 132.dp) {
    Box(
        modifier.size(size).clip(CircleShape).background(RadaPalette.DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Rada",
            modifier = Modifier.requiredSize(size * 1.3f)
        )
    }
}
