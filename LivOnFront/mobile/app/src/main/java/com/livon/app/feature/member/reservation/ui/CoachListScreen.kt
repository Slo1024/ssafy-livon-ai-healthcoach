// kotlin
// File: app/src/main/java/com/livon/app/feature/member/reservation/ui/CoachListScreen.kt
package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.card.CoachCard
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.Alignment.Companion.CenterVertically
import com.livon.app.feature.member.reservation.model.CoachUIModel


@Composable
fun CoachListScreen(
    coaches: List<CoachUIModel>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isCorporateUser: Boolean = false, // 기업 사용자 여부 플래그
    showLoadMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onCoachClick: (CoachUIModel) -> Unit = {}
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

            // 기업 사용자인 경우 상단 드롭다운 노출 (최상단 마진을 침범하지 않도록 content 영역에 배치)
            if (isCorporateUser) {
                CoachTypeDropdown(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .aspectRatio(288f / 30f)
                )
                Spacer(Modifier.height(12.dp))
            }
        },
        fullBleedContent = {
            // 드롭다운 선택값에 따라 필터링된 리스트를 사용하기 위해 내부 상태를 읽음
            val filterState = LocalCoachFilter.current
            val filtered = remember(coaches, filterState.selectedOption) {
                when (filterState.selectedOption) {
                    "전체" -> coaches
                    "기업 소속 코치" -> coaches.filter { it.isCorporate }
                    "일반 코치" -> coaches.filter { !it.isCorporate }
                    else -> coaches
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp) // 하단 버튼 가려짐 방지
            ) {
                items(filtered.take(10)) { coach ->
                    // Pass click handler via CoachCard's onClick parameter (more reliable than modifier.clickable)
                    CoachCard(
                        name = coach.name,
                        job = coach.job,
                        intro = coach.intro,
                        avatarUrl = coach.avatarUrl,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onCoachClick(coach) }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (showLoadMore) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = onLoadMore) {
                                Text(text = "다음 페이지 보기")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    )
}

/*
  간단한 드롭다운 구현:
  - 전체 / 기업 소속 코치 / 일반 코치 (선택값은 전역으로 공유하기 위해 CompositionLocal 사용)
  - 가로 꽉찬 aspectRatio(288/30) 사용 (요구한 288*30 비율 유지)
*/

private val LocalCoachFilter = compositionLocalOf { CoachFilterState() }

private class CoachFilterState {
    var selectedOption: String by mutableStateOf("전체")
    var expanded: Boolean by mutableStateOf(false)
}

@Composable
private fun CoachTypeDropdown(modifier: Modifier = Modifier) {
    // composition local 초기화 (상위에서 이미 사용중이면 덮어쓰지 않음)
    val current = remember { CoachFilterState() }
    CompositionLocalProvider(LocalCoachFilter provides current) {
        val options = listOf("전체", "기업 소속 코치", "일반 코치")
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { current.expanded = true },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = CenterVertically
            ) {
                Text(
                    text = current.selectedOption,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = current.expanded,
                onDismissRequest = { current.expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            current.selectedOption = option
                            current.expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "CoachListScreen")
@Composable
private fun PreviewCoachListScreen() = PreviewSurface {
    val mock = listOf(
        CoachUIModel(id = "1","김도윤", "피트니스 코치", "체형 분석 기반 근력·유산소 균형 프로그램", null, isCorporate = false),
        CoachUIModel(id = "2","박지성", "유산소 트레이너", "유산소 퍼포먼스 향상 및 빌드업 계획", null, isCorporate = true),
        CoachUIModel(id = "3","손흥민", "러닝 코치", "러닝 기술, 착지 개선, 인터벌 프로그램 설계", null, isCorporate = false),
        CoachUIModel(id = "4","이강인", "필라테스", "코어 강화, 밸런싱 중심 프로그램", null, isCorporate = true),
        CoachUIModel(id = "5","정우영", "영양 코치", "체형·목표에 맞는 식단 설계 및 점검", null, isCorporate = false),
        CoachUIModel(id = "6","황희찬", "근력 트레이너", "파워 및 근성 향상 집중 트레이닝", null, isCorporate = false),
        CoachUIModel(id = "7","김민재", "바디 리셋", "스트레칭→근력 밸런싱 리커버리 플랜", null, isCorporate = true),
        CoachUIModel(id = "8","조규성", "피트니스", "근력·유연성 균형 맞춤 루틴", null, isCorporate = false),
        CoachUIModel(id = "9","백승호", "필라테스", "골반·척추 정렬 중심 루틴", null, isCorporate = false),
        CoachUIModel(id = "10","이승우", "PT 코치", "1:1 자세 교정 집중 코칭", null, isCorporate = false),
        CoachUIModel(id = "11","권창훈", "영양", "식단 전략 설계·실행 지속케어", null, isCorporate = true),
        CoachUIModel(id = "12","안정환", "피트니스", "부상 예방 중심 트레이닝", null, isCorporate = false)
    )

    LivonTheme {
        CoachListScreen(
            coaches = mock,
            onBack = {},
            isCorporateUser = true,
            showLoadMore = true,
            onLoadMore = {},
            onCoachClick = {}
        )
    }
}