package com.rada.android.features.drive

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.rada.android.data.BatteryPromptPreferences
import com.rada.android.theme.RadaColors
import kotlinx.coroutines.launch

private fun isExempt(context: Context): Boolean =
    context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)

@Composable
fun BatteryOptimisationPrompt(enabled: Boolean) {
    val context = LocalContext.current
    val prefs = remember { BatteryPromptPreferences(context.applicationContext) }
    val shown by prefs.shown.collectAsState(initial = true)
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(enabled, shown) {
        if (enabled && !shown && !isExempt(context)) visible = true
    }
    if (!visible) return

    fun dismiss() {
        visible = false
        scope.launch { prefs.markShown() }
    }

    AlertDialog(
        onDismissRequest = { dismiss() },
        title = { Text("Keep Rada alerts running") },
        text = {
            Text(
                "Some phones stop apps that run in the background to save battery, which can silence your " +
                    "camera alerts while you use Maps or music. Set Rada to \"Unrestricted\" or " +
                    "\"Don't optimise\" so it keeps warning you."
            )
        },
        confirmButton = {
            TextButton(onClick = {
                dismiss()
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(intent) }
            }) { Text("Open settings", color = RadaColors.Blue) }
        },
        dismissButton = {
            TextButton(onClick = { dismiss() }) { Text("Not now", color = RadaColors.TextSecondary) }
        }
    )
}
