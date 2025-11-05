// com/livon/app/ui/component/input/CodeInputField.kt
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.theme.LivonTheme

/**
 * 이메일 인증코드 4자리 입력 필드
 * - 왼쪽 마진 20, 각 셀 W=40, 간격 7, 밑줄 stroke 1.6, color=primary
 * - 숫자만 입력, 최대 4자리, 붙여넣기 지원, 4자리 완료 시 onComplete 호출
 */
@Composable
fun CodeInputField(
    code: String,
    onCodeChange: (String) -> Unit,
    onComplete: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    cellCount: Int = 4
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    val handleChange: (String) -> Unit = { raw ->
        val filtered = raw.filter { it.isDigit() }.take(cellCount)
        onCodeChange(filtered)
        if (filtered.length == cellCount) onComplete(filtered)
    }

    BasicTextField(
        value = code,
        onValueChange = handleChange,
        singleLine = true,
        textStyle = TextStyle.Default, // 렌더링은 아래 Box의 Text로 처리
        cursorBrush = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = modifier,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(cellCount) { index ->
                    val ch = code.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .drawBehind {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (ch.isNotEmpty()) {
                            Text(
                                text = ch,
                                color = textColor,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}

/* ---------- Previews ---------- */
@Preview(showBackground = true, name = "CodeInputField - Empty")
@Composable
private fun PreviewCodeInputField_Empty() {
    LivonTheme {
        var code by remember { mutableStateOf("") }
        CodeInputField(code = code, onCodeChange = { code = it })
    }
}

@Preview(showBackground = true, name = "CodeInputField - Partial")
@Composable
private fun PreviewCodeInputField_Partial() {
    LivonTheme {
        var code by remember { mutableStateOf("12") }
        CodeInputField(code = code, onCodeChange = { code = it })
    }
}

@Preview(showBackground = true, name = "CodeInputField - Full")
@Composable
private fun PreviewCodeInputField_Full() {
    LivonTheme {
        var code by remember { mutableStateOf("1234") }
        CodeInputField(code = code, onCodeChange = { code = it })
    }
}
