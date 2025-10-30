// com/livon/app/feature/shared/auth/ui/HealthInfoConditionScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.button.SurveyOption
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.component.text.Topic
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun HealthInfoConditionScreen() {
    // 단일 선택 상태
    var selected by remember { mutableStateOf<String?>(null) }

    // 설문 옵션
    val options = listOf("고혈압", "천식", "당뇨", "심혈관 질환", "없음")

    CommonSignUpScreenB(
        title = "건강 상태 입력",
        onBack = {},
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = selected != null,
                onClick = { /* TODO: 다음 단계 이동 */ }
            )
        }
    ) {
        // ───────── Topic ─────────
        Topic("기저 질환")
        Spacer(Modifier.height(15.dp))

        // ───────── Requirement / Caption ─────────
        Column(
            modifier = Modifier.padding(start = 25.dp),
            horizontalAlignment = Alignment.Start
        ) {
            RequirementText("현재 앓고 있는 질환이 있나요?")
            Spacer(Modifier.height(3.dp))
            CaptionText("해당되는 항목을 선택해주세요")
            Spacer(Modifier.height(50.dp))
        }

        // ───────── Survey Buttons (1열 + Center) ─────────
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            options.forEach { label ->
                SurveyOption(
                    text = label,
                    selected = selected == label,
                    onClick = { selected = label }
                )
            }
        }
    }
}


/* ---------- Preview ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "HealthInfoConditionScreen")
@Composable
private fun PreviewHealthInfoConditionScreen() = PreviewSurface {
    HealthInfoConditionScreen()
}
