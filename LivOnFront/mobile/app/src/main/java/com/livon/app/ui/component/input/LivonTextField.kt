// com/livon/app/ui/component/input/LivonTextField.kt
package com.livon.app.ui.component.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.theme.LivonTheme

/**
 * 밑줄형 텍스트 필드 (이메일/비밀번호용)
 * - 폭 288dp, 좌우 마진 20dp
 * - 라벨(14 Medium onSurfaceVariant)
 * - 입력/플레이스홀더(18 Regular onBackground)
 * - 하단 밑줄: onSurfaceVariant, 0.8dp
 */
@Composable
fun LivonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy( // 18sp
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onBackground
    )
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp) // 좌우 20
            .width(288.dp)
    ) {
        // 라벨 (14 Medium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
        )

        Spacer(Modifier.height(6.dp))

        // 입력 영역 + 밑줄
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp) // 커서/텍스트가 라인 '위'에 보이도록 적당한 높이
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
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty() && !placeholder.isNullOrEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(color = placeholderColor)
                        )
                    }
                    inner()
                }
            )
        }
    }
}

/**
 * 과거 이름 호환용 (권장: LivonTextField 사용)
 */
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

/* ---------- Previews ---------- */
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
                placeholder = "example@example.com"
            )
            Spacer(Modifier.height(12.dp))
            LivonTextField(
                value = "",
                onValueChange = {},
                label = "비밀번호",
                placeholder = "********"
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
            placeholder = "example@example.com"
        )
    }
}
