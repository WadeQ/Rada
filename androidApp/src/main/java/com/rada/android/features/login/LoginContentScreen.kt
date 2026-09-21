package com.rada.android.features.login

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
fun LoginContentScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onSignIn: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onCreateAccount: () -> Unit,
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
        Spacer(Modifier.height(10.dp))
        Text("Rada", color = colors.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("kaa rada na ntsa", color = colors.onSurfaceVariant, fontSize = 12.5.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))

        Text("Welcome back", color = colors.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(14.dp))
        UsernameField(state.username, { onIntent(LoginIntent.UsernameChanged(it)) }, isError = state.errorMessage != null)
        Spacer(Modifier.height(12.dp))
        PasswordField(state.password, { onIntent(LoginIntent.PasswordChanged(it)) }, isError = state.errorMessage != null, onDone = onSignIn)
        if (state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.errorMessage, color = colors.error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(16.dp))
        SlideAuthAction("Slide to sign in", "Signing in…", busy = state.isBusy, enabled = state.username.isNotBlank() && state.password.isNotEmpty(), onConfirm = onSignIn)

        Spacer(Modifier.height(18.dp))
        OrDivider()
        Spacer(Modifier.height(16.dp))
        SocialAuthButtons(verb = "Sign in", enabled = !state.isBusy, onGoogle = onGoogle, onApple = onApple)

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("New to Rada?", color = colors.onSurfaceVariant, fontSize = 13.sp)
            TextButton(onClick = onCreateAccount) { Text("Create account", fontWeight = FontWeight.Bold, color = colors.onBackground) }
        }
        Spacer(Modifier.height(12.dp))
    }
}
