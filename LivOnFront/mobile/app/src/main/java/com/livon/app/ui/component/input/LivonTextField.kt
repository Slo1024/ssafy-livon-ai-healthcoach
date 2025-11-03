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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun LivonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    showCounter: Boolean = true,                        // ✅ 카운터 숨김 옵션
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None, // ✅ 패스워드 마스킹
    errorText: String? = null,                          // ✅ 에러 문구 슬롯
    trailingContent: (@Composable () -> Unit)? = null,  // ✅ 우측 아이콘/컨트롤
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Normal,
        color = Color(0xFF000000)
    )
) {
    val labelColor = Color(0xFF818286)
    val placeholderColor = Color(0xFFB5B6BD)
    val lineColor = Color(0xFFB5B6BD)
    val counterColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)

    Column(
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        // 라벨
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
        )

        Spacer(Modifier.height(6.dp))

        // 입력 + 밑줄
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
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (value.isEmpty() && !placeholder.isNullOrEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = textStyle.copy(color = placeholderColor)
                                )
                            }
                            inner()
                        }

                        // 우측 아이콘/컨트롤
                        if (trailingContent != null) {
                            Spacer(Modifier.width(8.dp))
                            trailingContent()
                        }

                        // 글자수 카운터 (옵션)
                        if (showCounter && maxLength != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${value.length}/$maxLength",
                                color = counterColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            )
        }

        // 에러 메시지 (있을 때만)
        if (!errorText.isNullOrEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}

/* --- 기존 프리뷰는 유지/생략 가능 --- */
