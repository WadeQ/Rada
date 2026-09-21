package com.rada.android.features.paywall

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.size
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
            listOf("Crowd checkpoints", "Longer warnings", "Alert history").forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = RadaColors.Accent, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(feature, color = RadaColors.TextSecondary, fontSize = 11.5.sp)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("How do you want to pay?", color = RadaColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

        val chosenPlan = state.plans[state.selectedPlanIndex]
        val yearly = chosenPlan.id == "annual"
        val playUsable = state.playStatus != PlayStatus.UNAVAILABLE

        if (state.payMethod == PayMethod.PLAY) {
            VisaStyleCard(
                selected = true,
                enabled = playUsable,
                periodLabel = if (yearly) "yearly" else "monthly",
                unavailableNote = null,
                onClick = {}
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Visa, Mastercard or M-Pesa through Google Play. Renews on its own; cancel any time in Google Play.",
                color = RadaColors.TextMuted, fontSize = 11.sp, lineHeight = 15.sp
            )
        } else {
            MethodStrip(
                brand = Brush.linearGradient(listOf(PayBrand.VisaNavy, PayBrand.VisaBlue)),
                mark = "VISA",
                title = "Card or M-Pesa via Google Play",
                subtitle = if (playUsable) "Auto-renews · Visa, Mastercard, GlobalPay" else "Not available on this device or build yet",
                enabled = playUsable,
                onClick = { onIntent(PaywallIntent.MethodSelected(PayMethod.PLAY)) }
            )
        }
        Spacer(Modifier.height(12.dp))
        if (state.payMethod == PayMethod.MPESA) {
            MpesaStyleCard(
                selected = true,
                amountLabel = "KES ${chosenPlan.amountKes}",
                periodLabel = if (yearly) "One year" else "30 days",
                onClick = {}
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Lipa na M-PESA: approve a prompt on your phone with your PIN. Pay again only when you choose to.",
                color = RadaColors.TextMuted, fontSize = 11.sp, lineHeight = 15.sp
            )
        } else {
            MethodStrip(
                brand = Brush.linearGradient(listOf(PayBrand.MpesaGreen, PayBrand.MpesaDeep)),
                mark = "M-PESA",
                title = "Lipa na M-PESA",
                subtitle = "Pay once · No auto-renew",
                enabled = true,
                onClick = { onIntent(PaywallIntent.MethodSelected(PayMethod.MPESA)) }
            )
        }
        Spacer(Modifier.height(14.dp))

        if (state.payMethod == PayMethod.MPESA) {
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
        } else {
            val plan = state.plans[state.selectedPlanIndex]
            val price = state.playPrices[plan.id] ?: plan.priceLabel
            Text(
                "$price, billed by Google Play",
                color = RadaColors.TextSecondary,
                fontSize = 12.sp
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

        if (state.payMethod == PayMethod.MPESA) {
            SlideToConfirm(
                label = "Slide to pay with M-Pesa",
                doneLabel = state.statusMessage ?: "Processing",
                accent = PayBrand.MpesaGreen,
                accentInk = Color.White,
                enabled = state.phoneInput.isNotBlank() && !state.isProcessing,
                onConfirm = { onIntent(PaywallIntent.PayTapped) }
            )
        } else {
            SlideToConfirm(
                label = "Slide to subscribe with Google Play",
                doneLabel = state.statusMessage ?: "Processing",
                accent = PayBrand.VisaBlue,
                accentInk = Color.White,
                enabled = state.playStatus == PlayStatus.AVAILABLE && !state.isProcessing,
                onConfirm = { onIntent(PaywallIntent.PlaySubscribeTapped) }
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Restore purchase",
            color = RadaColors.TextSecondary,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onIntent(PaywallIntent.RestoreTapped) }
                .padding(vertical = 8.dp)
        )
    }
}
