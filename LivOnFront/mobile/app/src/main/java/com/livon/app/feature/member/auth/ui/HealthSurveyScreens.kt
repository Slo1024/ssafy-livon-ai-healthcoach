// com/livon/app/feature/shared/auth/ui/HealthSurveyScreens.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.livon.app.ui.theme.LivonTheme

/* ---------------------------------------------------------
 * 공통: 단일 선택 설문 템플릿
 * - CommonSignUpScreenB를 사용
 * - 하단 버튼은 선택 시에만 활성화
 * --------------------------------------------------------- */
@Composable
private fun SingleChoiceSurveyScreen(
    title: String = "건강 상태 입력",
    topic: String,
    requirement: String,
    caption: String,
    options: List<String>,
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }

    CommonSignUpScreenB(
        title = title,
        onBack = onBack,
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = selected != null,
                onClick = { selected?.let(onNext) }
            )
        }
    ) {
        // ───────── Topic ─────────
        Topic(topic)
        Spacer(Modifier.height(15.dp))

        // ───────── Requirement / Caption ─────────
        Column(
            modifier = Modifier.padding(start = 25.dp),
            horizontalAlignment = Alignment.Start
        ) {
            RequirementText(requirement)
            Spacer(Modifier.height(3.dp))
            CaptionText(caption)
            Spacer(Modifier.height(50.dp))
        }

        // ───────── Survey Buttons (1열 + Center) ─────────
        Column(
            modifier = Modifier.fillMaxWidth(),
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

/* ---------------------------------------------------------
 * 1) 기저질환
 * --------------------------------------------------------- */
@Composable
fun HealthInfoConditionScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        topic = "기저 질환",
        requirement = "현재 앓고 있는 질환이 있나요?",
        caption = "해당되는 항목을 선택해주세요",
        options = listOf("고혈압", "천식", "당뇨", "심혈관 질환", "없음"),
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------------------------------------------------------
 * 2) 수면 상태
 * --------------------------------------------------------- */
@Composable
fun HealthInfoSleepQualityScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        topic = "수면 상태",
        requirement = "평소 수면 상태는 어떤가요?",
        caption = "가장 가까운 항목을 선택해주세요",
        options = listOf("숙면을 취함", "자주 깨거나 뒤척임", "잠이 잘 안옴", "불면증 진단 받음"),
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------------------------------------------------------
 * 3) 복약 여부
 * --------------------------------------------------------- */
@Composable
fun HealthInfoMedicationScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        topic = "복약 여부",
        requirement = "현재 복용 중인 약이 있나요?",
        caption = "복용 중인 항목이 있다면 선택해주세요",
        options = listOf("혈압약", "당뇨약", "정신 건강 관련 약물", "기타", "없음"),
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------------------------------------------------------
 * 4) 통증·불편함   ← (요구사항 수정 반영)
 * --------------------------------------------------------- */
@Composable
fun HealthInfoPainDiscomfortScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        topic = "통증•불편함",
        requirement = "몸에 불편함이 있는 부위가 있나요?",
        caption = "해당되는 부위를 선택해주세요.",
        options = listOf("허리", "무릎", "어깨/목", "손목", "기타", "없음"),
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------------------------------------------------------
 * 5) 스트레스/피로
 * --------------------------------------------------------- */
@Composable
fun HealthInfoStressScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        topic = "스트레스/피로",
        requirement = "스트레스/피로 정도를 선택해 주세요",
        caption = "하나를 선택해 주세요",
        options = listOf("거의 없음", "가끔 있음", "자주 있음", "심함"),
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------- Previews (각 화면 단독 미리보기) ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "1. 기저질환")
@Composable
private fun Preview_Condition() = PreviewSurface { LivonTheme { HealthInfoConditionScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "2. 수면상태")
@Composable
private fun Preview_Sleep() = PreviewSurface { LivonTheme { HealthInfoSleepQualityScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "3. 복약여부")
@Composable
private fun Preview_Medication() = PreviewSurface { LivonTheme { HealthInfoMedicationScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "4. 통증·불편함")
@Composable
private fun Preview_Pain() = PreviewSurface { LivonTheme { HealthInfoPainDiscomfortScreen() } }

@Preview(showBackground = true, showSystemUi = true, name = "5. 스트레스/피로")
@Composable
private fun Preview_Stress() = PreviewSurface { LivonTheme { HealthInfoStressScreen() } }
