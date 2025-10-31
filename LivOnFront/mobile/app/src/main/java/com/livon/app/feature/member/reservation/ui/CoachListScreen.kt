package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.Text

data class CoachUIModel(
    val name: String,
    val job: String?,
    val intro: String,
    val avatarUrl: String? // 현재는 null이면 ic_noprofile 사용 (CoachCard 내부 로직)
)

@Composable
fun CoachListScreen(
    coaches: List<CoachUIModel>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    CommonScreenC(
        topBar = { TopBar(title = "예약하기", onBack = onBack) },
        modifier = modifier
    ) {
        // ✅ TopBar 아래 안내 텍스트
        Text(
            text = "예약 가능한 코치",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(coaches) { coach ->
                CoachCard(
                    name = coach.name,
                    job = coach.job,
                    intro = coach.intro,
                    avatarUrl = coach.avatarUrl,
                    modifier = Modifier
                        .fillMaxWidth()                  // ✅ 전체 화면 가로
//                        .padding(horizontal = (-Spacing.Horizontal)) // ✅ CommonScreenC margin 무효 → 화면 끝까지
                )
            }
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "CoachListScreen")
@Composable
private fun PreviewCoachListScreen() = PreviewSurface {
    val mock = listOf(
        CoachUIModel(
            name = "김도윤",
            job = "피트니스 코치",
            intro = "유산소·근력 균형 트레이닝으로 체지방 감량과 근육 강화. 1:1 및 그룹 클래스 운영",
            avatarUrl = null
        ),
        CoachUIModel(
            name = "박서연",
            job = "영양 코치",
            intro = "체형·목표 맞춤 식단 설계 및 점검. 바쁜 직장인을 위한 간편 레시피 제공",
            avatarUrl = null
        ),
        CoachUIModel(
            name = "이정훈",
            job = "필라테스 코치",
            intro = "자세 교정과 코어 강화 중심 루틴. 재활 베이스의 무리 없는 프로그램",
            avatarUrl = null
        )
    )

    LivonTheme {
        CoachListScreen(
            coaches = mock,
            onBack = {}
        )
    }
}
