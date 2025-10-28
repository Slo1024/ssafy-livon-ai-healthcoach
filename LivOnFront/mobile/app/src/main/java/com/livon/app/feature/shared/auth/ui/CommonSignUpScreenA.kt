// com/livon/app/feature/shared/auth/ui/CommonSignUpScreenA.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
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
 * - TopBar 아래 → 요구사항 타이틀까지 Spacer 70dp
 * - 하단 공통 버튼 바(좌우 20dp, 하단 24dp)
 */
@Composable
fun CommonSignUpScreenA(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.Horizontal,
                        vertical = Spacing.BottomMargin
                    )
            ) { bottomBar() }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = Spacing.Horizontal)
        ) {
            Spacer(Modifier.height(Spacing.TopbarToTitle_A)) // 70dp
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
        // Text("요구사항", style = RequirementStyle()) 등…
    }
}
