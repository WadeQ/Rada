package com.rada.android.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun radaFieldBorder(): Color =
    if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color(0xFF9A2C50) else Color(0xFFC2496E)

@Composable
fun radaFieldColors(): TextFieldColors {
    val border = radaFieldBorder()
    val surface = MaterialTheme.colorScheme.surface
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = border,
        unfocusedBorderColor = border.copy(alpha = 0.75f),
        cursorColor = border,
        focusedContainerColor = surface,
        unfocusedContainerColor = surface,
        errorContainerColor = surface
    )
}
