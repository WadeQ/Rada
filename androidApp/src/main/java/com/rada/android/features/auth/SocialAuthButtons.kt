package com.rada.android.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rada.android.R
import com.rada.android.theme.radaFieldBorder

@Composable
fun SocialAuthButtons(
    verb: String,
    enabled: Boolean,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SocialButton("Google", "$verb with Google", R.drawable.ic_google_g, tint = null, enabled = enabled, onClick = onGoogle, modifier = Modifier.weight(1f))
        SocialButton("Apple", "$verb with Apple", R.drawable.ic_apple, tint = MaterialTheme.colorScheme.onBackground, enabled = enabled, onClick = onApple, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SocialButton(
    label: String,
    description: String,
    icon: Int,
    tint: Color?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, radaFieldBorder()),
        modifier = modifier.height(52.dp)
    ) {
        Icon(painterResource(icon), contentDescription = description, tint = tint ?: Color.Unspecified, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(line))
        Text(
            "or",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Box(Modifier.weight(1f).height(1.dp).background(line))
    }
}
