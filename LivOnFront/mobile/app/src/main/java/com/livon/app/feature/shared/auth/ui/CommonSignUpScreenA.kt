package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Spacing

/**
 * A그룹 화면 템플릿
 * - 기존 TopBar 사용
 * - StatusBar 포함 상단 여백 24dp 확보 후 TopBar 배치
 * - 하단 공통 버튼 바(좌우 20dp, 하단 24dp)
 */
@Composable
fun CommonSignUpScreenA(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        // Scaffold 기본 systemBars inset을 끄고 우리가 직접 준다(중복 방지)
        contentWindowInsets = WindowInsets(0),
        topBar = {
            // ✅ Status bar 영역 패딩 + 상단 마진 24dp 후 TopBar 배치
            Box(
                Modifier
//                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = Spacing.TopMargin)
            ) { topBar() }
        },
        bottomBar = {
            Box(
                Modifier
//                    .windowInsetsPadding(WindowInsets.navigationBars) // 제스처/네비게이션 바 포함
                    .padding(
                        horizontal = Spacing.Horizontal,
//                        vertical = Spacing.BottomMargin
                    )
            ) { bottomBar() }
        }
    ) { innerPadding: PaddingValues ->
        androidx.compose.foundation.layout.Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = Spacing.Horizontal)
        ) {
            // A그룹은 추가 스페이서는 여기서 필요 시 화면별로 넣기
            content()
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "CommonSignUpScreenA")
@Composable
private fun PreviewCommonSignUpScreenA() = PreviewSurface {
    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        // content
    }
}
