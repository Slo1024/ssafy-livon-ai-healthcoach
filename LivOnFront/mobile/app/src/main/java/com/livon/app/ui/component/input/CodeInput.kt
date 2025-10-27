package com.livon.app.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 4자리 인증 코드 표시용 간단 스텁
 * - 실제 입력은 BasicTextField로 구현 권장 (여기선 UI 프리뷰 용)
 */
@Composable
fun CodeInput(
    code: String,              // 최대 4자리
    cellCount: Int = 4
) {
    val shape = RoundedCornerShape(10.dp)
    val focusedColor = MaterialTheme.colorScheme.primary
    val idleColor = MaterialTheme.colorScheme.outline

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(cellCount) { idx ->
            val ch = code.getOrNull(idx)?.toString() ?: ""
            val borderColor = if (idx == code.length.coerceAtMost(cellCount - 1)) focusedColor else idleColor
            Text(
                text = ch,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, color = Color.Black),
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, borderColor, shape)
                    .background(Color.White, shape)
            )
        }
    }
}
