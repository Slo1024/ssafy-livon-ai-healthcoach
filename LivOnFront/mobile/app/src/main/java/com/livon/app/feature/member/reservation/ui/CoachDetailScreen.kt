package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R // ic_noprofile 때문에 추가
import com.livon.app.feature.member.reservation.model.CoachUIModel
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.calendar.MonthNavigator
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachDetailScreen(
    coach: CoachUIModel,
    onBack: () -> Unit,
    availableTimes: List<String> = listOf("09:00", "10:00", "11:00", "13:00", "14:00", "15:00"),
    onReserveNavigate: (selectedDate: LocalDate, selectedTime: String) -> Unit
) {
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var selectedTime by rememberSaveable { mutableStateOf<String?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val (morning, afternoon) = remember(availableTimes) {
        availableTimes.partition { it.substringBefore(":").toInt() < 12 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // 하단 버튼 영역 확보
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { TopBar(title = "상세 정보", onBack = onBack) }
            item { Spacer(Modifier.height(4.dp)) }

            // 프로필 이미지
            item {
                Image(
                    painter = painterResource(id = R.drawable.ic_noprofile), // TODO: 나중에 Coil 등으로 avatarUrl 로드
                    contentDescription = "코치 프로필 사진",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            item { Spacer(Modifier.height(10.dp)) }

            // 코치 이름
            item {
                Text(
                    text = "${coach.name} 코치",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                )
            }
            item { Spacer(Modifier.height(60.dp)) }

            // 코치 상세 정보 (LazyColumn 내에서 Column을 사용해 좌측 정렬)
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("코치 소개", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(16.dp))

                    CoachInfoRow(title = "직무", value = coach.job)
                    CoachInfoRow(title = "자격증", value = "자격증 1, 자격증 2") // TODO: 모델에 추가 필요
                    CoachInfoRow(title = "소개", value = coach.intro)
                    Spacer(Modifier.height(24.dp))
                }
            }

            // 달력
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    MonthNavigator(
                        yearMonth = currentMonth,
                        onPrev = { currentMonth = currentMonth.minusMonths(1) },
                        onNext = { currentMonth = currentMonth.plusMonths(1) }
                    )
                    CalendarMonth(
                        yearMonth = currentMonth,
                        selected = selectedDate,
                        onSelect = {
                            selectedDate = it
                            selectedTime = null // 날짜 변경 시 선택 시간 초기화
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item { Spacer(Modifier.height(20.dp)) }

            // 시간 선택
            if (selectedDate != null) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (morning.isNotEmpty()) {
                            Text("오전", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                morning.forEach { time ->
                                    TimeChipButton(
                                        time = time,
                                        isSelected = selectedTime == time,
                                        onClick = { selectedTime = time }
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        if (afternoon.isNotEmpty()) {
                            Text("오후", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                afternoon.forEach { time ->
                                    TimeChipButton(
                                        time = time,
                                        isSelected = selectedTime == time,
                                        onClick = { selectedTime = time }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }

        // 하단 예약 버튼
        PrimaryButtonBottom(
            text = "예약 하기",
            enabled = selectedDate != null && selectedTime != null,
            onClick = {
                if (selectedDate != null && selectedTime != null) {
                    onReserveNavigate(selectedDate!!, selectedTime!!)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CoachInfoRow(title: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
            color = Color.Gray
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun TimeChipButton(
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(width = 70.dp, height = 35.dp)
            .border(
                width = 0.8.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time.substringBeforeLast(':') + ":00", // 정각만 표시
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal),
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoachDetailScreenPreview() {
    LivonTheme {
        CoachDetailScreen(
            coach = CoachUIModel(
                id = "1",
                name = "김싸피",
                avatarUrl = null,
                job = "헬스 트레이너",
                intro = "헬스 전문 트레이너 입니다. 여러분의 건강을 책임지겠습니다. 함께 건강한 몸을 만들어봐요!"
            ),
            onBack = {},
            onReserveNavigate = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Time Chip Button Selected")
@Composable
private fun TimeChipButtonSelectedPreview() {
    LivonTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeChipButton(time = "10:00", isSelected = true, onClick = {})
            TimeChipButton(time = "11:00", isSelected = false, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Coach Info Row")
@Composable
private fun CoachInfoRowPreview() {
    LivonTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CoachInfoRow(title = "직무", value = "헬스 트레이너")
            CoachInfoRow(title = "자격증", value = "자격증 1, 자격증 2, 생활체육지도사...")
        }
    }
}
