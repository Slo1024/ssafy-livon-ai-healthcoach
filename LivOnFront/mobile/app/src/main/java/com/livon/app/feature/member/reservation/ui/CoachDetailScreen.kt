package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.livon.app.R
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.component.button.PrimaryButtonBottom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenA

@Composable
fun CoachDetailScreen(
    navController: NavController,
    coachName: String = "코치 홍길동",
    jobLabel: String = "직무",
    jobValue: String = "심리상담사",
    certLabel: String = "자격증",
    certValue: String = "상담사 1급",
    introLabel: String = "소개",
    introValue: String = "따뜻하고 전문적인 상담을 제공합니다.",
    onReserve: (selectedDate: String, selectedTime: String) -> Unit = { _, _ -> }
) {
    var selectedDateIndex by remember { mutableStateOf(0) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MM.dd (E)", Locale.KOREA)
    val dates = (0 until 14).map { today.plusDays(it.toLong()) }
    val dateLabels = dates.map { it.format(dateFormatter) }

    // 예시 가능한 시간 (실제 로직에 맞게 교체)
    val baseTimes = listOf("09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00")
    // 날짜별 가용성 샘플: 인덱스가 짝수면 모두 가능, 홀수면 일부만 가능
    val availableTimesForDate = { index: Int ->
        if (index % 2 == 0) baseTimes else baseTimes.mapIndexed { i, t -> if (i % 3 == 0) null else t }.filterNotNull()
    }

    CommonSignUpScreenA(
        topBar = { TopBar(title = "코치 예약", onBack = { navController.popBackStack() }) },
        bottomBar = {
            PrimaryButtonBottom(
                text = if (selectedTime == null) "예약하기" else "예약하기",
                onClick = {
                    selectedTime?.let { time ->
                        onReserve(dateLabels[selectedDateIndex], time)
                    }
                },
                enabled = selectedTime != null
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 프로필 이미지 및 이름 중앙 정렬
            Image(
                painter = painterResource(id = R.drawable.ic_noprofile),
                contentDescription = "코치 프로필",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = coachName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 정보 항목들
            InfoRow(label = jobLabel, value = jobValue)
            InfoRow(label = certLabel, value = certValue)
            InfoRow(label = introLabel, value = introValue, isMultiline = true)

            Spacer(modifier = Modifier.height(20.dp))

            // 달력(간단한 horizontal date picker)
            Text(text = "날짜 선택", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dateLabels) { label ->
                    val index = dateLabels.indexOf(label)
                    val selected = index == selectedDateIndex
                    Surface(
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (selected) 4.dp else 0.dp,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .clickable {
                                selectedDateIndex = index
                                selectedTime = null
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = label, fontSize = 14.sp, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "가능한 시간", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            val times = availableTimesForDate(selectedDateIndex)

            // 시간 그리드 (3열)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 300.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(times) { time ->
                    val isSelected = selectedTime == time
                    OutlinedButton(
                        onClick = { selectedTime = time },
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(text = time, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isMultiline: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = if (isMultiline) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachDetailScreen() {
    CoachDetailScreen(navController = rememberNavController())
}
