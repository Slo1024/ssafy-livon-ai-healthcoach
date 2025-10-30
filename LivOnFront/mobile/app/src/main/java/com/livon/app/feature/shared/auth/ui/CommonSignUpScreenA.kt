package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Spacing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding

/**
 * A그룹 화면 템플릿
 * - 기존 TopBar 사용
 * - 상단 여백 24dp 확보 후 TopBar 배치(상단 statusBars 패딩은 적용하지 않음)
 * - 하단 공통 버튼 바(좌우 20dp, 하단 24dp) + navigationBars 패딩
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
        topBar = {
            Box(
                Modifier
                    .padding(top = Spacing.TopMargin)
                    .padding(horizontal = Spacing.Horizontal)
            ) { topBar() }
        },
        bottomBar = {
            Box(
                Modifier
                    .padding(
                        horizontal = Spacing.Horizontal,
                    )
            ) { bottomBar() }
        }
    ) { inner ->
        androidx.compose.foundation.layout.Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = Spacing.Horizontal)
        ) {
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
