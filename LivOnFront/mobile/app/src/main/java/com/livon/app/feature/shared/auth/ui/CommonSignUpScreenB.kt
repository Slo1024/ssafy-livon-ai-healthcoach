package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.button.SurveyOption
import com.livon.app.ui.component.overlay.TopBar2
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.RequirementText
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
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        // ✅ 기본 인셋 사용 권장
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.TopMargin)
//                    .padding(horizontal = Spacing.Horizontal)
            ) {
                TopBar2(
                    title = title,
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars) // ✅ 하단 인셋

//                    .padding(
//                        horizontal = Spacing.Horizontal,
//                    )
            ) { bottomBar() }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = Spacing.Horizontal)
        ) {
            Spacer(Modifier.height(Spacing.TopbarToTitle_B))
            content()
        }
    }
}

@Preview(
    name = "CommonSignUpScreenB Preview",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun PreviewCommonSignUpScreenB() {
    PreviewSurface {
        CommonSignUpScreenB(
            title = "건강 정보 입력",
            onBack = {},
            bottomBar = {
                PrimaryButtonBottom(
                    text = "다음",
                    onClick = {},
                    enabled = true
                )
            }
        ) {
            // ✅ 실제 content가 잘 보이는지 테스트
            RequirementText("성별을 선택해주세요.")
            Spacer(Modifier.height(8.dp))
            CaptionText("입력하신 정보는 공개되지 않습니다.")
        }
    }
}


