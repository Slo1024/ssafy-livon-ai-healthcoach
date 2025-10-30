package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar2
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Spacing

/**
 * B그룹 화면 템플릿
 * - TopBar2 사용
 * - (현재 형식) StatusBar 인셋은 끄고, 상단 TopMargin(24dp) + 좌우 20dp 정렬
 * - TopBar2 아래 → 요구사항 타이틀까지 Spacer 30dp
 * - 하단 바는 좌우 20dp만 적용
 */
@Composable
fun CommonSignUpScreenB(
    title: String,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0), // 시스템 인셋 수동 관리
        topBar = {
            // ✅ TopBar 영역: 상단 24dp + 좌우 20dp + 가로 꽉 채우기
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.TopMargin)
                    .padding(horizontal = Spacing.Horizontal)
            ) {
                TopBar2(
                    title = title,
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth() // ✅ TopBar2도 폭을 꽉
                )
            }
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Horizontal)
            ) { bottomBar() }
        }
    ) { innerPadding: PaddingValues ->
        androidx.compose.foundation.layout.Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = Spacing.Horizontal)
        ) {
            // B 전용: TopBar2 아래 타이틀까지 간격
            Spacer(Modifier.height(Spacing.TopbarToTitle_B))
            content()
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "CommonSignUpScreenB")
@Composable
private fun PreviewCommonSignUpScreenB() = PreviewSurface {
    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "완료", onClick = {}) }
    ) {
        // content
    }
}
