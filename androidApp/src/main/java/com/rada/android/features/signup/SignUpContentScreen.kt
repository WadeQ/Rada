package com.rada.android.features.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.features.auth.OrDivider
import com.rada.android.features.auth.PasswordField
import com.rada.android.features.auth.SlideAuthAction
import com.rada.android.features.auth.SocialAuthButtons
import com.rada.android.features.auth.TwigaBadge
import com.rada.android.features.auth.UsernameField

@Composable
fun SignUpContentScreen(
    state: SignUpState,
    onIntent: (SignUpIntent) -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onHaveAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(16.dp))
        TwigaBadge(Modifier.align(Alignment.CenterHorizontally), size = 112.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Create your account",
            color = colors.onBackground, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        UsernameField(state.username, { onIntent(SignUpIntent.UsernameChanged(it)) }, isError = state.errorMessage != null)
        Spacer(Modifier.height(12.dp))
        PasswordField(state.password, { onIntent(SignUpIntent.PasswordChanged(it)) }, isError = state.errorMessage != null, onDone = onSubmit)
        if (state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.errorMessage, color = colors.error, fontSize = 12.sp)
        } else {
            Spacer(Modifier.height(6.dp))
            Text("At least 8 characters", color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        SlideAuthAction("Slide to create account", "Creating account…", busy = state.isBusy, enabled = state.username.isNotBlank() && state.password.isNotEmpty(), onConfirm = onSubmit)

        Spacer(Modifier.height(18.dp))
        OrDivider()
        Spacer(Modifier.height(16.dp))
        SocialAuthButtons(verb = "Sign up", enabled = !state.isBusy, onGoogle = onGoogle, onApple = onApple)

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", color = colors.onSurfaceVariant, fontSize = 13.sp)
            TextButton(onClick = onHaveAccount) { Text("Sign in", fontWeight = FontWeight.Bold, color = colors.onBackground) }
        }
        Spacer(Modifier.height(12.dp))
    }
}
