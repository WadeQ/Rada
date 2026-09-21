package com.rada.android.features.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.rada.android.theme.radaFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.SlideToConfirm
import com.rada.android.theme.RadaColors

@Composable
fun PaywallContentScreen(
    state: PaywallState,
    onIntent: (PaywallIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 22.dp, vertical = 30.dp)) {
        Text(
            "Go Pro",
            color = RadaColors.TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "One avoided fine pays for over five years.",
            color = RadaColors.TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(18.dp))

        state.plans.forEachIndexed { index, plan ->
            val selected = index == state.selectedPlanIndex
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) RadaColors.Blue.copy(alpha = 0.06f) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (selected) RadaColors.Blue else RadaColors.Ink3,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onIntent(PaywallIntent.PlanSelected(index)) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (plan.recommended) {
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RadaColors.Accent)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Best value",
                                color = RadaColors.AccentInk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        plan.name,
                        color = RadaColors.TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(plan.priceLabel, color = RadaColors.TextMuted, fontSize = 11.sp)
                }
                Text(
                    plan.perMonthLabel,
                    color = RadaColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        listOf(
            "Real-time driver-reported checkpoints",
            "Longer, speed-scaled warning distance",
            "Offline maps for upcountry trips",
            "Full alert history and trends"
        ).forEach { feature ->
            Row(Modifier.padding(vertical = 5.dp)) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = RadaColors.Blue,
                    modifier = Modifier.height(15.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(feature, color = RadaColors.TextSecondary, fontSize = 12.5.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        Text("M-Pesa number", color = RadaColors.TextSecondary, fontSize = 11.5.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("+254", color = RadaColors.TextSecondary, modifier = Modifier.padding(end = 8.dp))
            OutlinedTextField(
                value = state.phoneInput,
                onValueChange = { onIntent(PaywallIntent.PhoneChanged(it)) },
                placeholder = { Text("Enter M-Pesa number") },
                singleLine = true,
                isError = state.errorMessage != null,
                colors = radaFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.errorMessage, color = RadaColors.Red, fontSize = 12.sp)
        }
        if (state.statusMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.statusMessage, color = RadaColors.Blue, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))

        SlideToConfirm(
            label = "Slide to pay with M-Pesa",
            doneLabel = state.statusMessage ?: "Processing",
            enabled = state.phoneInput.isNotBlank() && !state.isProcessing,
            onConfirm = { onIntent(PaywallIntent.PayTapped) }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "You'll get an M-Pesa prompt on your phone to enter your PIN.",
            color = RadaColors.TextMuted, fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onIntent(PaywallIntent.RestoreTapped) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore purchase")
        }
    }
}
