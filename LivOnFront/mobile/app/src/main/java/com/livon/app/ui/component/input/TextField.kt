package com.livon.app.ui.component.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 라벨: 14 Medium #818286
 * 플레이스홀더: 18 Regular #B5B6BD
 * 텍스트: 18 Regular #000000
 * 포커스 아웃라인: primary, 기본: outline(border)
 * R10
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val labelColor = Color(0xFF818286)
    val placeholderColor = Color(0xFFB5B6BD)
    val textColor = Color(0xFF000000)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let {
            { Text(text = it, color = labelColor, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)) }
        },
        placeholder = placeholder?.let {
            { Text(text = it, color = placeholderColor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)) }
        },
        singleLine = true,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            errorTextColor = textColor
        ),
        textStyle = MaterialTheme.typography.titleMedium,
        isError = isError,
        modifier = modifier.fillMaxWidth()
    )
}
