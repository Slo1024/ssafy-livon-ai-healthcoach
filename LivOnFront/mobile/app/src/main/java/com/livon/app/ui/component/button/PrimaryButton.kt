package com.livon.app.ui.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 높이 44dp, R10
 * enabled=true  → bg = primary, text = onPrimary
 * enabled=false → bg = outline(또는 onSurfaceVariant), text = onPrimary (요구안: basic)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val colors = ButtonDefaults.buttonColors(
        containerColor = if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline, // ← 나중에 border/unclickable 토큰으로 매핑
        contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onPrimary
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = colors,
        modifier = modifier.fillMaxWidth().height(44.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
    }
}
