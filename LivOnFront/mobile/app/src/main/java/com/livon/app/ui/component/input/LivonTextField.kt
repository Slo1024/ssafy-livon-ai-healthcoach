// app/src/main/java/com/livon/app/ui/component/input/LivonTextField.kt
package com.livon.app.ui.component.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun LivonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default, // ← 추가(선택)
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Normal,
        color = Color(0xFF000000)
    )
) {
    val labelColor = Color(0xFF818286)
    val placeholderColor = Color(0xFFB5B6BD)
    val lineColor = Color(0xFFB5B6BD)

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
        )

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .drawBehind {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 0.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
        ) {
            BasicTextField(
                value = value,
                onValueChange = { input ->
                    val new = if (maxLength != null) input.take(maxLength) else input
                    onValueChange(new)
                },
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = if (maxLength != null) 8.dp else 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty() && !placeholder.isNullOrEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = textStyle.copy(color = placeholderColor)
                                )
                            }
                            inner()
                        }

                        if (maxLength != null) {
                            Text(
                                text = "${value.length}/$maxLength",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}

@Deprecated("Use LivonTextField instead")
@Composable
fun LivonLineInput(
    label: String,
    placeholder: String? = null,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) = LivonTextField(
    value = value,
    onValueChange = onValueChange,
    label = label,
    placeholder = placeholder,
    modifier = modifier
)

@Preview(showBackground = true, name = "LivonTextField - Empty")
@Composable
private fun PreviewLivonTextFieldEmpty() {
    LivonTheme {
        var v by remember { mutableStateOf("") }
        Column {
            LivonTextField(
                value = v,
                onValueChange = { v = it },
                label = "이메일",
                placeholder = "example@example.com",
                maxLength = 30
            )
            Spacer(Modifier.height(12.dp))
            LivonTextField(
                value = "",
                onValueChange = {},
                label = "비밀번호",
                placeholder = "********",
                maxLength = null
            )
        }
    }
}

@Preview(showBackground = true, name = "LivonTextField - Filled")
@Composable
private fun PreviewLivonTextFieldFilled() {
    LivonTheme {
        var v by remember { mutableStateOf("user@example.com") }
        LivonTextField(
            value = v,
            onValueChange = { v = it },
            label = "이메일",
            placeholder = "example@example.com",
            maxLength = 30
        )
    }
}
