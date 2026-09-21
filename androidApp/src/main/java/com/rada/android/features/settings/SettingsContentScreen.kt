package com.rada.android.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.BOTTOM_BAR_CLEARANCE
import com.rada.android.data.ThemeMode
import com.rada.android.screens.ProfileAvatar
import com.rada.shared.domain.WarningLead
import com.rada.android.theme.RadaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContentScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onPickPhoto: () -> Unit,
    onTestSound: () -> Unit = {},
    onHome: () -> Unit,
    onDrive: () -> Unit,
    onReport: () -> Unit,
    onHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var showAlertSheet by rememberSaveable { mutableStateOf(false) }

    if (showAlertSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlertSheet = false },
            containerColor = colors.surface
        ) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    "Alert settings",
                    color = colors.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
                )
                LeadRow(state.warningLead) { onIntent(SettingsIntent.WarningLeadSelected(it)) }
                RowDivider()
                ActionRow(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    title = "Test alert sound",
                    subtitle = "Plays the chime and voice warning at your current volume",
                    onClick = onTestSound
                )
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        Text(
            "Settings",
            color = colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp)
        )

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            PhotoHeader(state, onPickPhoto, onRemove = { onIntent(SettingsIntent.RemovePhotoTapped) })

            Spacer(Modifier.height(20.dp))
            NameGroup(state, onIntent)

            Spacer(Modifier.height(24.dp))
            GroupLabel("Preferences")
            SettingsGroup {
                ToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Alert notifications",
                    subtitle = "Camera and checkpoint warnings",
                    checked = state.alertsEnabled,
                    onToggle = { onIntent(SettingsIntent.AlertsToggled(it)) }
                )
                RowDivider()
                ActionRow(
                    icon = Icons.Outlined.Tune,
                    title = "Alert settings",
                    subtitle = "Warns about ${state.warningLead.seconds.toInt()} seconds ahead · test the sound",
                    showChevron = true,
                    onClick = { showAlertSheet = true }
                )
                RowDivider()
                val darkOn = when (state.themeMode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
                ToggleRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark mode",
                    checked = darkOn,
                    onToggle = { on ->
                        onIntent(SettingsIntent.ThemeModeSelected(if (on) ThemeMode.DARK else ThemeMode.LIGHT))
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
            SettingsGroup {
                ActionRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = "Log out",
                    subtitle = state.userName.takeIf { it.isNotBlank() }?.let { "Signed in as $it" } ?: "Sign out of this device",
                    tint = RadaColors.Accent,
                    onClick = { onIntent(SettingsIntent.SignOutTapped) }
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(BOTTOM_BAR_CLEARANCE))

    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface),
        content = content
    )
}

@Composable
private fun RowDivider(startIndent: Int = 52) {
    Box(
        Modifier
            .padding(start = startIndent.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
    )
}

@Composable
private fun PhotoHeader(state: SettingsState, onPick: () -> Unit, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(104.dp).clickable(onClick = onPick)) {
            ProfileAvatar(name = state.nameInput, photoPath = state.photoPath, size = 104.dp)
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(RadaColors.Accent)
                    .border(3.dp, colors.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.PhotoCamera,
                    "Change photo",
                    tint = RadaColors.AccentInk,
                    modifier = Modifier.size(17.dp))
            }
        }
        if (state.photoPath != null) {
            Text(
                "Remove photo",
                color = colors.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun NameGroup(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current

    SettingsGroup {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Display name", color = colors.onSurfaceVariant, fontSize = 14.sp)
            TextField(
                value = state.nameInput,
                onValueChange = { onIntent(SettingsIntent.NameInputChanged(it)) },
                singleLine = true,
                isError = state.nameError != null,
                placeholder = { Text("Display name", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                textStyle = TextStyle(textAlign = TextAlign.End, fontSize = 15.sp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = colors.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
    if (state.nameError != null) {
        Text(
            state.nameError,
            color = colors.error,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
    }
}

@Composable
private fun LeadRow(selected: WarningLead, onSelect: (WarningLead) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Timer, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Warning distance", color = colors.onSurface, fontSize = 15.sp)
                Text(
                    "About ${selected.seconds.toInt()} seconds ahead, up to ${selected.maxMeters.toInt()} m",
                    color = colors.onSurfaceVariant, fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().padding(start = 36.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WarningLead.entries.forEach { lead ->
                val on = lead == selected
                Box(
                    Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (on) RadaColors.Accent else colors.surfaceVariant)
                        .clickable { onSelect(lead) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        lead.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (on) RadaColors.AccentInk else colors.onSurface,
                        fontSize = 12.5.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color? = null,
    showChevron: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint ?: colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = tint ?: colors.onSurface, fontSize = 15.sp, fontWeight = if (tint != null) FontWeight.SemiBold else FontWeight.Normal)
            Text(subtitle, color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
        if (showChevron) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    subtitle: String? = null
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.onSurface, fontSize = 15.sp)
            if (subtitle != null) {
                Text(subtitle, color = colors.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RadaColors.AccentInk,
                checkedTrackColor = RadaColors.Accent,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = colors.onSurfaceVariant,
                uncheckedTrackColor = colors.surfaceVariant,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
