package com.rada.android.features.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.BOTTOM_BAR_CLEARANCE
import com.rada.android.theme.RadaColors
import com.rada.shared.data.MockData

@Composable
fun HelpContentScreen(
    state: HelpState,
    onIntent: (HelpIntent) -> Unit,
    onHome: () -> Unit,
    onDrive: () -> Unit,
    onReport: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 18.dp, vertical = 20.dp)) {
        Text(
            "Roadside help",
            color = RadaColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(RadaColors.Blue.copy(alpha = 0.14f))
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Text(
                "Free for every driver",
                color = RadaColors.Blue,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = BOTTOM_BAR_CLEARANCE)) {
            section(
                "Emergency",
                state.contacts.filter { it.category == MockData.ContactCategory.EMERGENCY },
                onIntent
            )
            section(
                "Tow companies nearby",
                state.contacts.filter { it.category == MockData.ContactCategory.TOW },
                onIntent
            )
            section(
                "Recommended mechanics",
                state.contacts.filter { it.category == MockData.ContactCategory.MECHANIC },
                onIntent
            )
        }

    }
}

private fun LazyListScope.section(
    title: String,
    contacts: List<MockData.Contact>,
    onIntent: (HelpIntent) -> Unit
) {
    item {
        Text(
            title,
            color = RadaColors.TextPrimary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
    items(contacts) { contact ->
        ContactRow(contact, onIntent)
    }
}

@Composable
private fun ContactRow(contact: MockData.Contact, onIntent: (HelpIntent) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(RadaColors.Ink2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Shield,
                contentDescription = null,
                tint = RadaColors.TextSecondary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                contact.name,
                color = RadaColors.TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(contact.meta, color = RadaColors.TextMuted, fontSize = 10.5.sp)
        }
        IconButton(
            onClick = { onIntent(HelpIntent.CallTapped(contact)) },
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(RadaColors.Accent)
        ) {
            Icon(
                Icons.Filled.Call,
                contentDescription = "Call ${contact.name}",
                tint = RadaColors.AccentInk,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
