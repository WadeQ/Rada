package com.rada.android.features.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpContentScreen(
    state: OtpState,
    onIntent: (OtpIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter the code", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(
            "We sent a 6-digit code to ${state.phoneDisplay}",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.code,
            onValueChange = { if (it.length <= 6) onIntent(OtpIntent.CodeChanged(it)) },
            placeholder = { Text("123456") },
            singleLine = true,
            isError = state.errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onIntent(OtpIntent.VerifyTapped) },
            enabled = !state.isVerifying,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isVerifying) "Verifying…" else "Verify")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { onIntent(OtpIntent.ResendTapped) }) {
            Text("Resend code")
        }
    }
}
