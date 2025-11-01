package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.card.CoachCard
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

data class CoachUIModel(
    val name: String,
    val job: String?,
    val intro: String,
    val avatarUrl: String?
)

@Composable
fun CoachListScreen(
    coaches: List<CoachUIModel>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier

) {
    CommonScreenC(
        topBar = { TopBar(title = "예약하기", onBack = onBack) },
        modifier = modifier,
        content = {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "예약 가능한 코치",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            )

            Spacer(Modifier.height(16.dp))
        },
        fullBleedContent = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 한 페이지 10개만 노출
                items(coaches.take(10)) { coach ->
                    CoachCard(
                        name = coach.name,
                        job = coach.job,
                        intro = coach.intro,
                        avatarUrl = coach.avatarUrl,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "CoachListScreen")
@Composable
private fun PreviewCoachListScreen() = PreviewSurface {
    val mock = listOf(
        CoachUIModel("김도윤", "피트니스 코치", "체형 분석 기반 근력·유산소 균형 프로그램", null),
        CoachUIModel("박지성", "유산소 트레이너", "유산소 퍼포먼스 향상 및 빌드업 계획", null),
        CoachUIModel("손흥민", "러닝 코치", "러닝 기술, 착지 개선, 인터벌 프로그램 설계", null),
        CoachUIModel("이강인", "필라테스", "코어 강화, 밸런싱 중심 프로그램", null),
        CoachUIModel("정우영", "영양 코치", "체형·목표에 맞는 식단 설계 및 점검", null),
        CoachUIModel("황희찬", "근력 트레이너", "파워 및 근성 향상 집중 트레이닝", null),
        CoachUIModel("김민재", "바디 리셋", "스트레칭→근력 밸런싱 리커버리 플랜", null),
        CoachUIModel("조규성", "피트니스", "근력·유연성 균형 맞춤 루틴", null),
        CoachUIModel("백승호", "필라테스", "골반·척추 정렬 중심 루틴", null),
        CoachUIModel("이승우", "PT 코치", "1:1 자세 교정 집중 코칭", null),
        CoachUIModel("권창훈", "영양", "식단 전략 설계·실행 지속케어", null),
        CoachUIModel("안정환", "피트니스", "부상 예방 중심 트레이닝", null)
    )

    LivonTheme {
        CoachListScreen(
            coaches = mock,
            onBack = {}
        )
    }
}

