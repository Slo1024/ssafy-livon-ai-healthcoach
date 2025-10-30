// com/livon/app/feature/shared/auth/ui/HealthInfoMedicationScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.component.button.SurveyOption
import com.livon.app.ui.preview.PreviewSurface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



@Composable
fun HealthInfoMedicationScreen() {
    // 🔹 단일 선택 상태를 템플릿 바깥(스크린 최상위)으로 올려야 bottomBar에서 읽을 수 있음
    var selected by remember { mutableStateOf<String?>(null) }
    val options = listOf("혈압약", "당뇨약", "정신건강 관련 약물", "기타", "없음")

    CommonSignUpScreenB(
        title = "건강 상태 입력",
        onBack = {},
        bottomBar = {
            // 🔹 선택 완료 시에만 활성화
            PrimaryButtonBottom(text = "다음", enabled = selected != null, onClick = { /* TODO */ })
        }
    ) {
        // ── 상단 안내 영역 ───────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(start = 25.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(2.dp))            // TopBar2 아래 약간의 여백
            Text("Topic")                             // Topic (spec: TopBar2 바로 아래 정도)
            Spacer(Modifier.height(15.dp))
            RequirementText("복약 여부를 알려주세요")
            Spacer(Modifier.height(3.dp))
            CaptionText("하나를 선택해 주세요")
            Spacer(Modifier.height(16.dp))
        }

        // ── 설문 옵션 영역 ───────────────────────────────────────────────
        // 버튼 W220 x H50 규격. 1열/2열은 디자인에 맞게 배치하세요.
        Column(
            modifier = Modifier
                .padding(start = 25.dp) // 왼쪽 마진 25
        ) {
            // 간단히 2열 배치 예시(원하면 FlowRow로 바꿔도 됨)
            for (row in options.chunked(2)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { label ->
                        SurveyOption(
                            text = label,
                            selected = selected == label,
                            onClick = { selected = label },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewHealthInfoMedicationScreen() = PreviewSurface { HealthInfoMedicationScreen() }


