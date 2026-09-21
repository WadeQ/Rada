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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    var menuOpen by remember { mutableStateOf(false) }
    var menuShown by remember { mutableStateOf(false) }
    val avatarScale by animateFloatAsState(if (menuShown) 0.94f else 1f, spring(dampingRatio = 0.6f), label = "avatarScale")

    fun closeMenu(after: () -> Unit = {}) {
        menuShown = false
        scope.launch {
            delay(MENU_EXIT_MS)
            menuOpen = false
            after()
        }
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(104.dp)) {
            Box(Modifier.graphicsLayer { scaleX = avatarScale; scaleY = avatarScale }) {
                ProfileAvatar(name = state.userName, photoPath = state.photoPath, size = 104.dp)
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(RadaColors.Accent)
                    .border(3.dp, colors.background, CircleShape)
                    .clickable {
                        if (menuOpen) closeMenu() else {
                            menuOpen = true
                            menuShown = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = menuShown, animationSpec = tween(180), label = "badgeIcon") { open ->
                    Icon(
                        if (open) Icons.Outlined.Close else Icons.Outlined.PhotoCamera,
                        if (open) "Close" else "Edit profile photo",
                        tint = RadaColors.AccentInk,
                        modifier = Modifier.size(17.dp)
                    )
                }
                if (menuOpen) {
                    val gap = with(LocalDensity.current) { 12.dp.roundToPx() }
                    Popup(
                        alignment = Alignment.TopStart,
                        offset = IntOffset(with(LocalDensity.current) { 34.dp.roundToPx() } + gap, -with(LocalDensity.current) { 22.dp.roundToPx() }),
                        onDismissRequest = { closeMenu() },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PhotoAction(
                                visible = menuShown,
                                delayMs = 0,
                                icon = Icons.Outlined.Edit,
                                label = if (state.photoPath != null) "Change photo" else "Choose photo",
                                iconTint = RadaColors.AccentInk,
                                iconBackground = RadaColors.Accent,
                                onClick = { closeMenu(onPick) }
                            )
                            if (state.photoPath != null) {
                                PhotoAction(
                                    visible = menuShown,
                                    delayMs = 70,
                                    icon = Icons.Outlined.Delete,
                                    label = "Remove photo",
                                    iconTint = colors.error,
                                    iconBackground = colors.error.copy(alpha = 0.16f),
                                    onClick = { closeMenu(onRemove) }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (state.userName.isNotBlank()) {
            Text(
                state.userName,
                color = colors.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun PhotoAction(
    visible: Boolean,
    delayMs: Int,
    icon: ImageVector,
    label: String,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160, delayMs)) +
            scaleIn(spring(dampingRatio = 0.55f, stiffness = 380f), initialScale = 0.5f, transformOrigin = TransformOrigin(0f, 0.5f)) +
            slideInHorizontally(tween(220, delayMs)) { -it / 4 },
        exit = fadeOut(tween(120)) + scaleOut(tween(140), targetScale = 0.6f, transformOrigin = TransformOrigin(0f, 0.5f))
    ) {
        Row(
            Modifier
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.onSurface.copy(alpha = 0.08f), CircleShape)
                .clickable(onClick = onClick)
                .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(label, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private const val MENU_EXIT_MS = 170L

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
