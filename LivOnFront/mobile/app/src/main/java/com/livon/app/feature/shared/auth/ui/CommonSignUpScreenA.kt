package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp

/**
 * A그룹 화면 템플릿
 * - 기존 TopBar 사용
 * - 상단 여백 24dp 확보 후 TopBar 배치(상단 statusBars 패딩은 적용하지 않음)
 * - 하단 공통 버튼 바(좌우 20dp, 하단 24dp) + navigationBars 패딩
 */
@Composable
fun CommonSignUpScreenA(
    topBar: @Composable (Modifier) -> Unit,     // ✅ modifier 받도록 타입 변경
    bottomBar: @Composable (Modifier) -> Unit,  // ✅ modifier 받도록 타입 변경
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Box(
                Modifier
                    .padding(top = Spacing.TopMargin)
//                    .padding(horizontal = Spacing.Horizontal)
                    .fillMaxWidth()                                 // ✅ 가로 꽉
            ) {
                topBar(Modifier.fillMaxWidth())                     // ✅ 전달한 컴포넌트도 꽉
            }
        },
        bottomBar = {
            Box(
                Modifier
//                    .padding(horizontal = Spacing.Horizontal)
                    .fillMaxWidth()                                 // ✅ 가로 꽉
                    .windowInsetsPadding(WindowInsets.navigationBars) // ✅ 제스처 바 겹침 방지
            ) {
                bottomBar(Modifier.fillMaxWidth())                  // ✅ 전달한 컴포넌트도 꽉
            }
        }
    ) { inner ->
        androidx.compose.foundation.layout.Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = Spacing.Horizontal)
        ) {
            Spacer(Modifier.height(80.dp)) // TopBar 아래 공통 80dp
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
