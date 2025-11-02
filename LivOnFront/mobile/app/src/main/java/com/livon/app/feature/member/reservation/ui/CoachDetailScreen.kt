package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.feature.member.reservation.model.CoachUIModel
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenA
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.calendar.MonthNavigator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachDetailScreen(
    coach: CoachUIModel,
    onBack: () -> Unit,
    availableTimes: List<String> = listOf("09:00","10:00","11:00","13:00","14:00","15:00"),
    onReserve: (selectedDate: LocalDate, selectedTime: String) -> Unit
) {
    var step by rememberSaveable { mutableStateOf(0) }              // 0: 날짜 -> 1: 시간
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) } // 🔹 LocalDate로 변경
    var selectedTime by rememberSaveable { mutableStateOf<String?>(null) }

    val (morning, afternoon) = remember(availableTimes) {
        availableTimes.partition { it.substringBefore(":").toInt() < 12 }
    }

    var currentMonth by rememberSaveable { mutableStateOf(YearMonth.now()) }

    CommonSignUpScreenA(
        topBar = { TopBar(title = coach.name, onBack = onBack) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // 코치 간단 정보
                if (!coach.job.isNullOrBlank()) {
                    Text(
                        text = coach.job ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold, fontSize = 18.sp
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    text = coach.intro,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                if (step == 0) {
                    // 🔹 달력 + 네비게이터
                    MonthNavigator(
                        yearMonth = currentMonth,
                        onPrev = { currentMonth = currentMonth.minusMonths(1) },
                        onNext = { currentMonth = currentMonth.plusMonths(1) },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    CalendarMonth(
                        yearMonth = currentMonth,
                        selected = selectedDate,
                        onSelect = { selectedDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("시간 선택", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))

                    if (morning.isNotEmpty()) {
                        Text("오전", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            maxItemsInEachRow = 3,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            morning.forEach { t ->
                                TimeChip(
                                    label = t,
                                    selected = selectedTime == t,
                                    onClick = { selectedTime = t }
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    if (afternoon.isNotEmpty()) {
                        Text("오후", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            maxItemsInEachRow = 3,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            afternoon.forEach { t ->
                                TimeChip(
                                    label = t,
                                    selected = selectedTime == t,
                                    onClick = { selectedTime = t }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }
        },
        bottomBar = {
            val enabled = if (step == 0) selectedDate != null else selectedTime != null
            val buttonText = if (step == 0) "선택" else "예약 하기"
            PrimaryButtonBottom(
                text = buttonText,
                enabled = enabled,
                onClick = {
                    if (step == 0 && selectedDate != null) {
                        step = 1
                        selectedTime = null
                    } else if (step == 1 && selectedDate != null && selectedTime != null) {
                        onReserve(selectedDate!!, selectedTime!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

/* 공통 칩 버튼 */
@Composable
private fun TimeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 78.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}


@Preview(showBackground = true, showSystemUi = false)
@Composable
fun CoachDetailScreenPreview() {
    val sample = CoachUIModel(
        id = "coach_001",
        name = "홍길동",
        job = "퍼스널 코치",
        intro = "경력 5년, 체력 관리 및 맞춤형 운동 프로그램 전문.",
        avatarUrl = null,
        isCorporate = false
    )
    CoachDetailScreen(
        coach = sample,
        onBack = {},
        availableTimes = listOf("09:00","10:00","11:00","13:00","14:00","15:00"),
        onReserve = { _, _ -> }
    )
}
