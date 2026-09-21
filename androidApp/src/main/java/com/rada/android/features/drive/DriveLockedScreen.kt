package com.rada.android.features.drive

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.BOTTOM_BAR_CLEARANCE
import com.rada.android.theme.RadaColors

@Composable
fun DriveLockedScreen(onUpgrade: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(RadaColors.Accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Navigation, null, tint = RadaColors.AccentInk, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Your free trial has ended",
            color = RadaColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Drive is the heart of Rada. Go Pro to keep getting warned before cameras, checkpoints and alcohol checks.",
            color = RadaColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        listOf("Live Drive map with your car", "Alerts, even while you use other apps", "Checkpoint and alcohol-check warnings").forEach {
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, null, tint = RadaColors.Blue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(it, color = RadaColors.TextPrimary, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
        Button(
            onClick = onUpgrade,
            colors = ButtonDefaults.buttonColors(containerColor = RadaColors.Accent, contentColor = RadaColors.AccentInk),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Go Pro · KES 200/month", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(BOTTOM_BAR_CLEARANCE + 40.dp))
    }
}
