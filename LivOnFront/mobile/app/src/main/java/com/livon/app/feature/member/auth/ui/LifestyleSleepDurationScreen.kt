// com/livon/app/feature/shared/auth/ui/LifestyleSleepDurationScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.button.SurveyOption
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.component.text.Topic
import com.livon.app.ui.preview.PreviewSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun LifestyleSleepDurationScreen() {
    // 01~12 목록
    val hours = remember { (1..12).map { it.toString().padStart(2, '0') } }

    var selectedIndex by remember { mutableStateOf(6) } // 0-based


    CommonSignUpScreenB(
        title = "생활습관 입력",
        onBack = {},
        bottomBar = {
            // 이 화면의 규격: 버튼은 항상 활성화
            PrimaryButtonBottom(
                text = "다음",
                enabled = true,
                onClick = {
                    val selectedHour = selectedIndex
                    // TODO: 선택값 사용
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(start = 25.dp),
            horizontalAlignment = Alignment.Start
        ) {
            RequirementText("하루 평균 수면시간은 몇 시간인가요?")
            Spacer(Modifier.height(3.dp))
            CaptionText("대략적인 시간으로 알려주세요")
            Spacer(Modifier.height(20.dp))

        }

        // ───────── Wheel Picker ─────────
        val itemHeight = 44.dp
        val visibleCount = 5 // 중앙 포함하여 위/아래 2개씩 보이게
        val pickerHeight = itemHeight * visibleCount
        val density = LocalDensity.current
        val listState = rememberLazyListState(
            // 중앙에 오도록 약간 앞에 배치
            initialFirstVisibleItemIndex = (selectedIndex - 2).coerceAtLeast(0)
        )
        val flingBehavior = rememberSnapFlingBehavior(listState)

        // 스크롤 변화를 감지해서 "가장 중앙에 가까운" 항목으로 선택 업데이트
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect { (idx, offsetPx) ->
                    val itemPx = with(density) { itemHeight.toPx() }
                    val delta = (offsetPx / itemPx).toFloat()
                    val centerIndex = (idx + delta).roundToInt() + 2 // 위에 2칸 보정
                    selectedIndex = centerIndex.coerceIn(0, hours.lastIndex)
                }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()                 // 남는 영역 사용
                .wrapContentHeight(Alignment.CenterVertically),   // ★ 세로 중앙 정렬
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 가운데 하이라이트 배경 + 숫자 리스트
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
                    // 중앙 하이라이트 박스(연한 배경)
                    Box(
                        Modifier
                            .fillMaxWidth(fraction = 0.4f) // 숫자 폭에 맞춰 적당히
                            .height(itemHeight * 2)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )

                    // 숫자 리스트 (스냅)
                    DisableSelection {
                        LazyColumn(
                            state = listState,
                            flingBehavior = flingBehavior,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(vertical = (pickerHeight - itemHeight) / 2)
                        ) {
                            items(hours.size) { i ->
                                val isSelected = (i == selectedIndex)
                                Text(
                                    text = hours[i],
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .height(itemHeight)
                                        .wrapContentHeight(),
                                    textAlign = TextAlign.Center,
                                    // ── 숫자 스타일(선택/비선택) ──
                                    style = if (isSelected)
                                        MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,                            // ★ 더 진한 볼드
                                            fontSize = 50.sp,
                                            color = MaterialTheme.colorScheme.onBackground           // ★ 진한 색
                                        )
                                    else
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 50.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy( // ★ 연한 숫자
                                                alpha = 0.6f
                                            )
                                        )
                                )

                            }
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // "시간" 라벨
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewLifestyleSleepDurationScreen() = PreviewSurface {
    LifestyleSleepDurationScreen()
}