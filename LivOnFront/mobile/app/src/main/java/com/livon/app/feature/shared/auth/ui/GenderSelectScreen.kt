// com/livon/app/feature/shared/auth/ui/GenderSelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.ChoiceButtonCard
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Spacing

@Composable
fun GenderSelectScreen(modifier: Modifier = Modifier) {
    var selectedGender by remember { mutableStateOf<String?>(null) }

    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = {},
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = selectedGender != null,
                onClick = {}
            )
        }
    ) {
        // TopBar2 아래 Spacer(30dp)는 템플릿에서 제공
        Column(modifier = Modifier.fillMaxWidth()) {
            RequirementText("성별을 선택해주세요.")
            androidx.compose.foundation.layout.Spacer(Modifier.height(30.dp))
            CaptionText("입력하신 정보는 공개되지 않습니다.\n코칭 맞춤 분석을 위해 필요합니다.")
        }

        Spacer(
            Modifier
                .height(100.dp)
        )
        // 가운데 높이에 2개 카드가 양옆으로 배치되도록 Box 사용
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .offset(y = (-40).dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ChoiceButtonCard(
                        text = "남성",
                        selected = selectedGender == "M",
                        onClick = { selectedGender = "M" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(135f / 160f) // 비율 유지
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ChoiceButtonCard(
                        text = "여성",
                        selected = selectedGender == "F",
                        onClick = { selectedGender = "F" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(135f / 160f) // 비율 유지
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenderSelectScreenPreview() = PreviewSurface { GenderSelectScreen() }