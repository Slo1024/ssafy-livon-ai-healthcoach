// com/livon/app/ui/theme/TextStyles.kt
package com.livon.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun RequirementStyle(
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight = FontWeight.Medium
): TextStyle = MaterialTheme.typography.titleLarge.copy(
    fontWeight = fontWeight,
    color = color
)

@Composable
fun CaptionStyle(
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight: FontWeight = FontWeight.Medium
): TextStyle = MaterialTheme.typography.bodySmall.copy(
    fontWeight = fontWeight,
    color = color
)
