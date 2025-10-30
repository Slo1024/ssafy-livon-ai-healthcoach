// com/livon/app/ui/component/button/PrimaryButton.kt
package com.livon.app.ui.component.button

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.theme.Border
import com.livon.app.ui.theme.LivonTheme

@Composable
fun PrimaryButtonCore(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val colors = ButtonDefaults.buttonColors(
        containerColor = if (enabled) MaterialTheme.colorScheme.primary
        else Border,
        contentColor = Color(0xFFFFFFFF) // Basic color
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = colors,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            ),
            color = Color(0xFFFFFFFF) // Basic color
        )
    }
}

@Composable
fun PrimaryButtonBottom(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    horizontalMargin: Dp = 20.dp,
    bottomMargin: Dp = 24.dp,
    targetWidth: Dp = 288.dp,
    height: Dp = 44.dp,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(
//                start = horizontalMargin,
//                end = horizontalMargin,
                bottom = bottomMargin,
                top = bottomMargin,
            )
    ) {
        val maxW = maxWidth
        val available = (maxW - horizontalMargin)
        val buttonWidth: Dp = available.coerceAtMost(targetWidth)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            PrimaryButtonCore(
                text = text,
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .width(buttonWidth)
                    .height(height)
            )
        }
    }
} // ← 함수 닫힘 (원본 로직 그대로, 누락된 중괄호만 보완)

/* ---------- Previews ---------- */

@Preview(showBackground = true, showSystemUi = true, name = "PrimaryButtonBottom - Enabled")
@Composable
private fun Preview_PrimaryButtonBottom_Enabled() {
    LivonTheme {
        // 버튼이 잘 보이도록 연회색 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
        ) {
            PrimaryButtonBottom(
                text = "다음",
                onClick = {},
                enabled = true,
                horizontalMargin = 20.dp,
                bottomMargin = 24.dp,
                targetWidth = 288.dp,
                height = 44.dp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "PrimaryButtonBottom - Disabled")
@Composable
private fun Preview_PrimaryButtonBottom_Disabled() {
    LivonTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
        ) {
            PrimaryButtonBottom(
                text = "다음",
                onClick = {},
                enabled = false,   // Border(#BABABA) 확인
                horizontalMargin = 20.dp,
                bottomMargin = 24.dp,
                targetWidth = 288.dp,
                height = 44.dp
            )
        }
    }
}
