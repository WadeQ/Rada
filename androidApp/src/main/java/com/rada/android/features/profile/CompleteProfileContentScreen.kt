package com.rada.android.features.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import com.rada.android.theme.radaFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.SlideToConfirm

@Composable
fun CompleteProfileContentScreen(
    state: CompleteProfileState,
    onIntent: (CompleteProfileIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))
        Text(
            "Choose a username",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "This is how Rada greets you. It's the last thing we'll ask.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.nameInput,
            onValueChange = { onIntent(CompleteProfileIntent.NameChanged(it)) },
            placeholder = { Text("Username") },
            singleLine = true,
            isError = state.errorMessage != null,
            colors = radaFieldColors(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(20.dp))

        SlideToConfirm(
            label = "Slide to continue",
            doneLabel = "Saved",
            enabled = state.nameInput.isNotBlank(),
            onConfirm = { onIntent(CompleteProfileIntent.SaveTapped) }
        )
    }
}
