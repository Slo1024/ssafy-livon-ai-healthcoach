package com.livon.app.feature.shared.auth.ui


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Spacing

/**
 * B그룹 템플릿
 * - 기존 TopBar 사용
 * - 하단 버튼 없음
 * - 상단 24dp + TopBar
 * - 내용 시작 전 80dp spacer 적용
 */
@Composable
fun CommonScreenC(
    topBar: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Box(
                Modifier.run {
                    padding(top = Spacing.TopMargin)
                                .fillMaxWidth()
                }
            ) {
                topBar(Modifier.fillMaxWidth())
            }
        }
    ) { inner ->
        Column(
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
@Preview(showBackground = true, showSystemUi = true, name = "CommonScreenC")
@Composable
private fun PreviewCommonScreenC() = PreviewSurface {
    CommonScreenC(
        topBar = { TopBar(title = "예약하기", onBack = {}) },
    ) {
        // content
    }
}
