package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Spacing

@Composable
fun CommonScreenC(
    topBar: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    fullBleedContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier,
        topBar = {
            Box(
                Modifier
                    .padding(top = Spacing.TopMargin)
                    .fillMaxWidth()
            ) {
                topBar(Modifier.fillMaxWidth())
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 기본 패딩 콘텐츠
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Horizontal)
            ) {
                content()
            }

            // 가로 풀 콘텐츠
            fullBleedContent?.let {
                val bottom = inner.calculateBottomPadding()
                Spacer(Modifier.height(8.dp))
                // Give the full-bleed content a weight so it receives a finite height
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = bottom)
                ) { it() }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "CommonScreenC")
@Composable
private fun PreviewCommonScreenC() = PreviewSurface {
    CommonScreenC(
        topBar = { TopBar(title = "예약하기", onBack = {}) },
        content = {},
        fullBleedContent = {}
    )
}
