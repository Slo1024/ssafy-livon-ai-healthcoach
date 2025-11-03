package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.livon.app.R
import com.livon.app.feature.member.reservation.model.CoachUIModel
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.calendar.MonthNavigator
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.PaddingValues

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = { TopBar(title = "상세 정보", onBack = onBack) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "예약 하기",
                enabled = selectedDate != null && selectedTime != null,
                onClick = {
                    if (selectedDate != null && selectedTime != null) {
                        onReserveNavigate(selectedDate!!, selectedTime!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_noprofile),
                contentDescription = "코치 프로필 사진",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(10.dp))

            Text(
                text = "${coach.name} 코치",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            )
            Spacer(Modifier.height(24.dp))

            // 소개 및 상세 정보
            Column(Modifier.fillMaxWidth()) {
                Text("코치 소개", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(16.dp))

                CoachInfoRow(title = "직무", value = coach.job)
                CoachInfoRow(title = "자격증", value = "자격증 1, 자격증 2")
                CoachInfoRow(title = "소개", value = coach.intro)
            }
            Spacer(Modifier.height(24.dp))

            // 달력 (소개 아래로)
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
                    selectedTime = null
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            // 시간 선택 (선택된 날짜가 있을 때만 표시)
            if (selectedDate != null) {
                Column(
                    Modifier
                        .fillMaxWidth()
                ) {
                    if (morning.isNotEmpty()) {
                        Text("오전", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
                        Spacer(Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 88.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentPadding = PaddingValues(0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = rememberLazyGridState()
                        ) {
                            items(morning) { time ->
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

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 88.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentPadding = PaddingValues(0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = rememberLazyGridState()
                        ) {
                            items(afternoon) { time ->
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

            Spacer(Modifier.height(80.dp)) // 하단 버튼 영역 여유
        }
    }
}

@Composable
private fun CoachInfoRow(title: String, value: String?) {
    if (value.isNullOrBlank()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun TimeChipButton(time: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = time,
            color = if (isSelected) Color.White else Color.Black,
            style = MaterialTheme.typography.bodyMedium
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

@Preview(showBackground = true, name = "Time Chip Groups - Grid")
@Composable
private fun TimeChipGroupsGridPreview() {
    LivonTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("오전", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 88.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val morningTimes = listOf("08:00", "09:00", "10:00", "11:00")
                items(morningTimes) { time ->
                    TimeChipButton(time = time, isSelected = time == "09:00", onClick = {})
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("오후", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 88.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val afternoonTimes = listOf("13:00", "14:00", "15:00", "16:00")
                items(afternoonTimes) { time ->
                    TimeChipButton(time = time, isSelected = time == "15:00", onClick = {})
                }
            }
        }
    }
}
