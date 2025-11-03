// com/livon/app/feature/shared/auth/ui/LifestyleSurveyScreens.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.button.SurveyOption
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.NoticeTextSmall
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface
import kotlin.math.roundToInt

/* ---------------------------------------------------------
 * 공통 단일 선택 템플릿
 *  - CommonSignUpScreenB를 사용 (프로젝트 내 기존 템플릿)
 *  - '다음'은 선택될 때만 활성화
 * --------------------------------------------------------- */
@Composable
private fun SingleChoiceSurveyScreen(
    title: String = "생활습관 입력",
    requirement: String,
    caption: String,
    options: List<String>,
    verticalGap: Dp = 30.dp,            // 옵션 간 간격 (화면별 튜닝)
    headerBottomSpace: Dp = 50.dp,      // 질문/캡션 아래 여유 공간
    extraTopSpacer: Dp = 0.dp,          // 옵션 시작 전 추가 간격
    extraBottom: (@Composable () -> Unit)? = null, // ← 수신자 제거
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
        // 상단 질문 영역
        Column(
            modifier = Modifier.padding(start = 25.dp),
            horizontalAlignment = Alignment.Start
        ) {
            RequirementText(requirement)
            Spacer(Modifier.height(3.dp))
            CaptionText(caption)
            Spacer(Modifier.height(headerBottomSpace))
        }

        if (extraTopSpacer > 0.dp) Spacer(Modifier.height(extraTopSpacer))

        // 옵션 리스트
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(verticalGap)
        ) {
            options.forEach { label ->
                SurveyOption(
                    text = label,
                    selected = selected == label,
                    onClick = { selected = label }
                )
            }
        }

        // 하단 추가 영역(안내문 등)
        Spacer(Modifier.height(16.dp))
        extraBottom?.invoke() // ← 그냥 호출
    }
}


/* ---------------------------------------------------------
 * 1) 흡연 여부 (첫 화면)
 *  - 옵션 간격 50dp, 상단 추가 여백 90dp (기존 화면 레이아웃 반영)
 * --------------------------------------------------------- */
@Composable
fun LifestyleSmokingScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        requirement = "흡연 여부를 알려주세요",
        caption = "현재 상태를 선택해주세요",
        options = listOf("비흡연", "흡연", "금연"),
        verticalGap = 50.dp,
        headerBottomSpace = 50.dp,
        extraTopSpacer = 90.dp,
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------------------------------------------------------
 * 2) 음주 빈도
 *  - 하단 안내문 "정확하지 않아도 괜찮아요"
 *  - 옵션 간격 30dp, 상단 추가 여백 70dp
 * --------------------------------------------------------- */
@Composable
fun LifestyleAlcoholIntakeScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        requirement = "평소 음주는 얼마나 하시나요?",
        caption = "가장 가깝운 항목을 선택해주세요",
        options = listOf("하지 않음", "가끔 마심(월 1~2회)", "주 1~2회", "거의 매일"),
        verticalGap = 30.dp,
        headerBottomSpace = 50.dp,
        extraTopSpacer = 70.dp,
        extraBottom = {
            Spacer(Modifier.height(30.dp))
            NoticeTextSmall("정확하지 않아도 괜찮아요")
        },
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------------------------------------------------------
 * 3) 수면 시간 (휠 피커)
 *  - 버튼 항상 활성화 (기존 화면 동작)
 *  - 숫자 하이라이트/스냅 유지
 * --------------------------------------------------------- */
@Composable
fun LifestyleSleepDurationScreen(
    onBack: () -> Unit = {},
    onNext: (selectedHour: String) -> Unit = {}
) {
    val hours = remember { (1..12).map { it.toString().padStart(2, '0') } }

    var selectedIndex by rememberSaveable { mutableStateOf(6) } // 초기 07
    val itemHeight = 70.dp
    val visibleCount = 3
    val pickerHeight = itemHeight * visibleCount

    CommonSignUpScreenB(
        title = "생활습관 입력",
        onBack = onBack,
        bottomBar = {
            PrimaryButtonBottom(text = "다음", enabled = true, onClick = {
                onNext(hours[selectedIndex])
            })
        }
    ) {
        Column(Modifier.padding(start = 25.dp), horizontalAlignment = Alignment.Start) {
            RequirementText("하루 평균 수면시간은 몇 시간인가요?")
            Spacer(Modifier.height(3.dp))
            CaptionText("대략적인 시간으로 알려주세요")
            Spacer(Modifier.height(10.dp))
        }

        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0) // 3개 보일 때 중앙 보정값=1
        )
        val flingBehavior = rememberSnapFlingBehavior(listState)
        val density = LocalDensity.current

        // ★ 중앙선에 가장 가까운 아이템을 선택으로 갱신
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .collect { items ->
                    if (items.isEmpty()) return@collect
                    val centerY = listState.layoutInfo.viewportStartOffset +
                            (with(density) { pickerHeight.toPx() } / 2f)
                    val nearest = items.minByOrNull { item ->
                        val mid = item.offset + item.size / 2f
                        kotlin.math.abs(mid - centerY)
                    }
                    nearest?.let { selectedIndex = it.index.coerceIn(0, hours.lastIndex) }
                }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .wrapContentHeight(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pickerHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(pickerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    // ▶ 회색 하이라이트(더 뚜렷) + 테두리
                    Box(
                        Modifier
                            .width(130.dp)
                            .height(itemHeight)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = com.livon.app.ui.theme.Border, // border_black 대신 쓰는 색상
                                shape = RoundedCornerShape(14.dp)
                            )
                    )

                    DisableSelection {
                        LazyColumn(
                            state = listState,
                            flingBehavior = flingBehavior,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(
                                vertical = (pickerHeight - itemHeight) / 2
                            )
                        ) {
                            items(hours.size) { i ->
                                val isSelected = (i == selectedIndex)

                                val color = if (isSelected)
                                    com.livon.app.ui.theme.Border // = border_black
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

                                Text(
                                    text = hours[i],
                                    modifier = Modifier
                                        .height(itemHeight)
                                        .wrapContentHeight(Alignment.CenterVertically),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Normal, // Regular
                                        fontSize = 50.sp,
                                        color = color
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "시간",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}





/* ---------------------------------------------------------
 * 4) 활동 수준
 *  - 옵션 간격 40dp, 상단 추가 여백 70dp
 * --------------------------------------------------------- */
@Composable
fun LifestyleActivityLevelScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        requirement = "평소 활동 수준은 어떤가요?",
        caption = "비슷한 척도의 활동 수준을 골라주세요.",
        options = listOf(
            "거의 움직이지 않음",
            "가사, 출퇴근, 가벼운 산책",
            "하루 대부분 앉아서 생활",
            "주 1~2회 가벼운 운동",
            "주 3회 이상 운동"
        ),
        verticalGap = 40.dp,
        headerBottomSpace = 20.dp,
        extraTopSpacer = 70.dp,
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------------------------------------------------------
 * 5) 카페인 섭취
 *  - 기존 함수명 보존: LifestyleCaffeinIntakeScreen (철자 그대로)
 *  - 하단 안내문 포함
 * --------------------------------------------------------- */
@Composable
fun LifestyleCaffeinIntakeScreen(
    onBack: () -> Unit = {},
    onNext: (selected: String) -> Unit = {}
) {
    SingleChoiceSurveyScreen(
        requirement = "평소 카페인 섭취는 얼마나 하시나요?",
        caption = "커피, 에너지 음료 등을 포함합니다.",
        options = listOf("거의 안 마심", "하루 1잔 정도", "하루 2~3잔", "하루 4잔 이상"),
        verticalGap = 30.dp,
        headerBottomSpace = 50.dp,
        extraTopSpacer = 70.dp,
        extraBottom = {
            Spacer(Modifier.height(30.dp))
            NoticeTextSmall("정확하지 않아도 괜찮아요")
        },
        onBack = onBack,
        onNext = onNext
    )
}

/* ---------- Previews (각 화면 단독) ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "1. 흡연 여부")
@Composable
private fun PreviewSmoking() = PreviewSurface { LifestyleSmokingScreen() }

@Preview(showBackground = true, showSystemUi = true, name = "2. 음주 빈도")
@Composable
private fun PreviewAlcohol() = PreviewSurface { LifestyleAlcoholIntakeScreen() }

@Preview(showBackground = true, showSystemUi = true, name = "3. 수면 시간")
@Composable
private fun PreviewSleepDuration() = PreviewSurface { LifestyleSleepDurationScreen() }

@Preview(showBackground = true, showSystemUi = true, name = "4. 활동 수준")
@Composable
private fun PreviewActivityLevel() = PreviewSurface { LifestyleActivityLevelScreen() }

@Preview(showBackground = true, showSystemUi = true, name = "5. 카페인 섭취")
@Composable
private fun PreviewCaffeine() = PreviewSurface { LifestyleCaffeinIntakeScreen() }
