package com.rada.android.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.OnboardingCameraAlertIllustration
import com.rada.android.screens.SlideToConfirm

/**
 * Pure content: takes [state], emits [LoginIntent]s upward, knows nothing about the
 * ViewModel, Scaffold, or navigation. Firebase's Activity-dependent submit() call and
 * navigation both live in LoginRouteScreen — this file should stay unit-testable with a
 * fake state and a no-op lambda.
 */
@Composable
fun LoginContentScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onSubmitTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 22.dp)) {

        Column(
            Modifier
                .weight(1.1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OnboardingCameraAlertIllustration(Modifier.fillMaxWidth(0.75f))
            Spacer(Modifier.height(12.dp))
            Text(
                "Rada",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "kaa rada na ntsa",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.5.sp
            )
        }

        Text(
            "Welcome back",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Sign in to sync saved routes and alerts.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.5.sp
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "+254",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            OutlinedTextField(
                value = state.phoneInput,
                onValueChange = { onIntent(LoginIntent.PhoneChanged(it)) },
                placeholder = { Text("Enter phone number") },
                singleLine = true,
                isError = state.errorMessage != null,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(16.dp))

        if (state.isValidating) {
            Text(
                "Sending code…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        } else {
            SlideToConfirm(
                label = "Slide to sign in",
                doneLabel = "Sending code",
                enabled = state.phoneInput.isNotBlank(),
                onConfirm = onSubmitTapped
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("Google") }
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("Apple") }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "By continuing you agree to the terms and privacy policy.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}