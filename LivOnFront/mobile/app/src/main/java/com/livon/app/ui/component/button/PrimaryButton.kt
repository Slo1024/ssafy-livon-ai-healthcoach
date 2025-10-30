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
// com/livon/app/ui/component/button/PrimaryButton.kt
@Composable
fun PrimaryButtonBottom(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    horizontalMargin: Dp = 20.dp,
    bottomMargin: Dp = 24.dp,
    targetWidth: Dp = 288.dp,
    height: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    // bottomBar는 "세로 wrap"이어야 함
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()                 // 가로만 꽉
                .wrapContentHeight()            // ★ 세로는 wrap
                .navigationBarsPadding()        // 하단 시스템바 피하기
                .padding(
                    start = horizontalMargin,
                    end = horizontalMargin,
                    bottom = bottomMargin
                ),
            contentAlignment = Alignment.Center
        ) {
            // 버튼은 "최대 targetWidth, 최소 가로폭=남은 공간" 형태로
            PrimaryButtonCore(
                text = text,
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = targetWidth) // 화면이 좁으면 줄어들고, 넓으면 targetWidth로 제한
                    .height(height)
            )
        }
    }
}


@Preview(
    name = "PrimaryButtonBottom – Just Button",
    backgroundColor = 0xFFFFFFFF, // 흰 배경
    showSystemUi = false          // 상태바/네비바 숨김
)
@Composable
private fun Preview_PrimaryButtonBottom_JustButton() {
    LivonTheme {
        // 작은 캔버스에 버튼만 가운데 표시
        Box(
            modifier = Modifier
                .width(360.dp)          // 프리뷰 캔버스 가로
                .wrapContentHeight()    // 세로는 버튼 높이만
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            PrimaryButtonBottom(
                text = "다음",
                onClick = {},
                enabled = true
            )
        }
    }
}

@Preview(
    name = "PrimaryButtonBottom – Disabled (Just Button)",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    showSystemUi = false
)
@Composable
private fun Preview_PrimaryButtonBottom_JustButton_Disabled() {
    LivonTheme {
        Box(
            modifier = Modifier
                .width(360.dp)
                .wrapContentHeight()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            PrimaryButtonBottom(
                text = "다음",
                onClick = {},
                enabled = false
            )
        }
    }


}

