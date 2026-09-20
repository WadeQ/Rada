package com.rada.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.theme.RadaColors

private data class ObStep(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: androidx.compose.ui.graphics.Color?, // null = theme primary
    val title: String,
    val body: String
)

private val steps = listOf(
    ObStep(
        Icons.Filled.CameraAlt, RadaColors.Amber, "Cameras, before you reach them",
        "A starting set of known enforcement corridors, plus checkpoints and cameras reported by drivers like you. Locations are approximate — not officially confirmed by NTSA."
    ),
    ObStep(
        Icons.Filled.Shield,
        null,
        "What gets shared, and what doesn't",
        "Reporting a camera or checkpoint uploads its location so other drivers can see it too. Your live position while driving isn't stored or shared — only the point you choose to report."
    ),
    ObStep(
        Icons.Filled.LocationOn,
        RadaColors.Amber,
        "Location, even when closed",
        "Alerts fire while Rada runs in the background — this needs location access to keep working."
    )
)

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val current = steps[step]
    val tint = current.tint ?: androidx.compose.material3.MaterialTheme.colorScheme.primary

    Column(modifier = modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= step) RadaColors.Accent else RadaColors.Ink3)
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (step == 0) {
                OnboardingCameraAlertIllustration(Modifier.fillMaxWidth(0.85f))
                Spacer(Modifier.height(16.dp))
            } else {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(current.icon, contentDescription = null, tint = tint)
                }
                Spacer(Modifier.height(16.dp))
            }
            Text(
                current.title,
                color = RadaColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                current.body,
                color = RadaColors.TextSecondary,
                fontSize = 13.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (step == 2) {
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier
                        .background(RadaColors.Ink2, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "You can turn this off anytime in Settings — alerts just won't work while the app is closed.",
                        color = RadaColors.TextMuted, fontSize = 11.5.sp
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }, modifier = Modifier.height(48.dp)) {
                    Text("Back")
                }
            }
            Button(
                onClick = { if (step < 2) step++ else onDone() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RadaColors.Accent,
                    contentColor = RadaColors.AccentInk
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Continue")
            }
        }
    }
}