package com.rada.android.features.paywall

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
        .verticalScroll(rememberScrollState())
        .animateContentSize()
        .padding(horizontal = 22.dp, vertical = 24.dp)) {
        Text("Go Pro", color = RadaColors.TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text("One avoided fine pays for years of Pro.", color = RadaColors.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            state.plans.forEachIndexed { index, plan ->
                val selected = index == state.selectedPlanIndex
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) RadaColors.Accent.copy(alpha = 0.08f) else Color.Transparent)
                        .border(1.dp, if (selected) RadaColors.Accent else RadaColors.Ink3, RoundedCornerShape(16.dp))
                        .clickable { onIntent(PaywallIntent.PlanSelected(index)) }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(plan.name, color = RadaColors.TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        if (plan.recommended) {
                            Text(
                                "Best value",
                                color = RadaColors.AccentInk,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(RadaColors.Accent).padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(plan.perMonthLabel, color = RadaColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(plan.priceLabel, color = RadaColors.TextMuted, fontSize = 10.5.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Full Drive access", "Checkpoint alerts", "Alcohol check alerts").forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = RadaColors.Accent, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(feature, color = RadaColors.TextSecondary, fontSize = 11.5.sp)
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        val chosenPlan = state.plans[state.selectedPlanIndex]
        val yearly = chosenPlan.id == "annual"
        val playUsable = state.playStatus != PlayStatus.UNAVAILABLE

        VisaStyleCard(
            selected = true,
            enabled = playUsable,
            periodLabel = if (yearly) "yearly" else "monthly",
            unavailableNote = if (playUsable) null else "Not available on this device or build yet",
            onClick = {}
        )
        Spacer(Modifier.height(5.dp))
        Text(
            "Visa, Mastercard or M-PESA GlobalPay through Google Play. Renews on its own; cancel any time in Google Play.",
            color = RadaColors.TextMuted, fontSize = 11.sp, lineHeight = 15.sp
        )
        Spacer(Modifier.height(14.dp))

        val price = state.playPrices[chosenPlan.id] ?: chosenPlan.priceLabel
        Text(
            "$price, billed by Google Play",
            color = RadaColors.TextSecondary,
            fontSize = 12.sp
        )
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
            label = "Slide to subscribe with Google Play",
            doneLabel = state.statusMessage ?: "Processing",
            enabled = state.playStatus == PlayStatus.AVAILABLE && !state.isProcessing,
            onConfirm = { onIntent(PaywallIntent.PlaySubscribeTapped) }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onIntent(PaywallIntent.RestoreTapped) },
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text("Restore purchase", fontSize = 13.sp)
        }
    }
}
