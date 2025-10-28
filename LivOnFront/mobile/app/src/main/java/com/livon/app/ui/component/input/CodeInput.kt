// com/livon/app/ui/component/input/CodeInput.kt
package com.livon.app.ui.component.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.theme.LivonTheme

/**
 * CodeInput: 4개의 밑줄 표시
 * - 각 라인: W=40dp, stroke=1.6dp, color=primary(Main)
 * - 라인 간 간격: 7dp
 * - 왼쪽 마진: 20dp
 * - 높이: 24dp (라인이 하단에 그려짐)
 */
@Composable
fun CodeInput(
    code: String = "",
    cellCount: Int = 4,
) {
    val lineColor = MaterialTheme.colorScheme.primary

    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier.padding(start = 20.dp)
    ) {
        repeat(cellCount) {
            Canvas(
                modifier = Modifier
                    .size(width = 40.dp, height = 24.dp)
            ) {
                // 여기서는 Density 컨텍스트 안이므로 toPx() 사용 가능 ✅
                drawLine(
                    color = lineColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "CodeInput (default)")
@Composable
private fun PreviewCodeInputDefault() {
    LivonTheme {
        CodeInput()
    }
}


