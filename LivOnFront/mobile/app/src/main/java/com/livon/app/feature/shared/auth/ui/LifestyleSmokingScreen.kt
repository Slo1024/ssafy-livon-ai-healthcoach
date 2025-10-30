// com/livon/app/feature/shared/auth/ui/LifestyleSmokingScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.button.SurveyOption
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.component.text.Topic
import com.livon.app.ui.preview.PreviewSurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LifestyleSmokingScreen() {
    // 🔹 단일 선택 상태를 템플릿 바깥(스크린 최상위)으로 올려야 bottomBar에서 읽을 수 있음
    var selected by remember { mutableStateOf<String?>(null) }
    val options = listOf("비흡연", "흡연", "금연")

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
//        Topic("스트레스/피로")
//        Spacer(Modifier.height(15.dp))

        // ───────── Requirement / Caption ─────────
        Column(
            modifier = Modifier.padding(start = 25.dp),
            horizontalAlignment = Alignment.Start
        ) {
            RequirementText("흡연 여부를 알려주세요")
            Spacer(Modifier.height(3.dp))
            CaptionText("현재 상태를 선택해주세요")
            Spacer(Modifier.height(50.dp))
        }

        // ───────── Survey Buttons (1열 + Center) ─────────
        Spacer(Modifier.height(90.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(50.dp)
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
@Preview(showBackground = true)
@Composable
private fun PreviewLifestyleSmokingScreen() = PreviewSurface { LifestyleSmokingScreen() }


