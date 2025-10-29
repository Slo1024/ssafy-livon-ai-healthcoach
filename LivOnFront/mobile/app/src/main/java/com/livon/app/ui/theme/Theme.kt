package com.livon.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color.kt에 정의한 토큰을 그대로 사용
private val LightColorScheme = lightColorScheme(
    primary          = Main,
    onPrimary        = Color.White,
    secondary        = Sub1,
    onSecondary      = Color.White,
    background       = Basic,
    onBackground     = Gray2,
    surface          = Basic,
    onSurface        = Gray2,
    surfaceVariant   = Sub2,
    onSurfaceVariant = Gray,
    outline          = Border,
    error            = LiveRed,
    onError          = Color.White
)

@Composable
fun LivonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,   // 👈 라이트 한 벌 고정
        typography  = Typography,         // Pretendard 적용된 Type.kt
        content     = content
    )
}
