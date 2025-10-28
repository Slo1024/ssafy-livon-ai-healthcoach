// com/livon/app/ui/component/input/LivonTextField.kt
package com.livon.app.ui.component.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.theme.LivonTheme

/**
 * 이메일 / 비밀번호 입력 라인
 * - W=288, Color=Gray (onSurfaceVariant), Stroke=0.8dp
 * - 좌우 Margin=20dp
 * - 텍스트는 위에 배치
 */
@Composable
fun LivonLineInput(
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .width(288.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(Modifier.height(4.dp))
        Canvas(modifier = Modifier.height(24.dp)) {
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height),
                end = Offset(288.dp.toPx(), size.height),
                strokeWidth = 0.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        placeholder?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "LivonLineInput")
@Composable
private fun PreviewLivonLineInput() {
    LivonTheme {
        Column {
            LivonLineInput(label = "이메일", placeholder = "example@example.com")
            Spacer(Modifier.height(12.dp))
            LivonLineInput(label = "비밀번호", placeholder = "********")
        }
    }
}
