// com/livon/app/feature/shared/auth/ui/CommonSignUpScreenB.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
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
 * - TopBar2 아래 → 요구사항 타이틀까지 Spacer 30dp
 */
@Composable
fun CommonSignUpScreenB(
    title: String,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBar2(title = title, onBack = onBack) },
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
            Spacer(Modifier.height(Spacing.TopbarToTitle_B)) // 30dp
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
        // Text("요구사항", style = RequirementStyle()) 등…
    }
}
