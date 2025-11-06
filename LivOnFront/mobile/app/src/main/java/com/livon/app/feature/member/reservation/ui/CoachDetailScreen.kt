package com.livon.app.feature.member.reservation.ui

import android.net.Uri
import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.livon.app.R
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenA
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.navbar.HomeNavBar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import com.livon.app.feature.member.reservation.model.CoachUIModel

@Composable
fun CoachDetailScreen(
    navController: NavHostController,
    coach: CoachUIModel? = null,
    coachName: String = "코치 홍길동",
    jobLabel: String = "직무",
    jobValue: String = "심리상담사",
    certLabel: String = "자격증",
    certValue: String = "상담사 1급",
    introLabel: String = "소개",
    introValue: String = "따뜻하고 전문적인 상담을 제공합니다.",
    showSchedule: Boolean = true
) {
    // Prefer values from coach model when provided
    val displayName = coach?.name ?: coachName
    val displayJob = coach?.job ?: jobValue
    val displayIntro = coach?.intro ?: introValue

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()

    // sample times: AM and PM (include 12:00 in PM list as requested)
    val amTimes = listOf("09:00", "10:00", "11:00")
    val pmTimes = listOf("12:00", "13:00", "14:00", "15:00", "16:00", "17:00")

    var yearMonth by remember { mutableStateOf(YearMonth.from(today)) }

    CommonSignUpScreenA(
        topBar = { TopBar(title = "코치 예약", onBack = { navController.popBackStack() }) },
        bottomBar = {
            if (showSchedule) {
                PrimaryButtonBottom(
                    text = "예약하기",
                    onClick = {
                        val dateStr = selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: ""
                        val encodedCoach = Uri.encode(displayName)
                        if (selectedDate != null && selectedTime != null) {
                            navController.navigate("qna_submit/$encodedCoach/$dateStr")
                        }
                    },
                    enabled = selectedTime != null
                )
            } else {
                // For group mode, show only HomeNavBar
                HomeNavBar(
                    modifier = Modifier.fillMaxWidth(),
                    currentRoute = null,
                    navController = navController,
                    onNavigate = { /* fallback noop */ }
                )
            }
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

            // Profile icon size: 1.5x of previous 90 -> 135.dp
            Image(
                painter = painterResource(id = R.drawable.ic_noprofile),
                contentDescription = "코치 프로필",
                modifier = Modifier
                    .size(135.dp)
                    .clip(RoundedCornerShape(67.5.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(67.5.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 정보 항목들
            InfoRow(label = jobLabel, value = displayJob)
            InfoRow(label = certLabel, value = certValue)
            InfoRow(label = introLabel, value = displayIntro, isMultiline = true)

            Spacer(modifier = Modifier.height(20.dp))

            if (showSchedule) {
                // Month selector header with prev/next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                        Text(text = "<")
                    }
                    Text(
                        text = yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월", Locale.KOREA)),
                        style = MaterialTheme.typography.titleSmall
                    )
                    TextButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                        Text(text = ">")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // CalendarMonth component to pick a date
                CalendarMonth(
                    yearMonth = yearMonth,
                    selected = selectedDate,
                    onSelect = { date ->
                        selectedDate = date
                        selectedTime = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // If a date is selected, show AM/PM time pickers
                if (selectedDate != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "오전", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeGrid(times = amTimes, selectedTime = selectedTime) { t -> selectedTime = t }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "오후", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeGrid(times = pmTimes, selectedTime = selectedTime) { t -> selectedTime = t }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TimeGrid(times: List<String>, selectedTime: String?, onSelect: (String) -> Unit) {
    // Responsive 3-column grid where button width is computed from available width so labels won't be cut.
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = 8.dp
        val columns = 3
        val maxBtnWidth = 300.dp // increased max width to allow longer labels
        val btnHeight = 40.dp // increased height so labels look better
        val totalGap = gap * (columns - 1)
        val parentW = this.maxWidth
        val calculated = (parentW - totalGap) / columns
        val buttonWidth = if (calculated > maxBtnWidth) maxBtnWidth else calculated

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                items(times) { t ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val isSelected = selectedTime == t
                        // Keep border unchanged; only change container and content colors
                        val constantBorder = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                        OutlinedButton(
                            onClick = { onSelect(t) },
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(btnHeight),
                            shape = RoundedCornerShape(6.dp),
                            border = constantBorder,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(text = t, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        )
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

@Composable
fun CoachDetailScreenNav(nav: NavHostController, coach: CoachUIModel?, showSchedule: Boolean) {
    CoachDetailScreen(navController = nav, coach = coach, showSchedule = showSchedule)
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachDetailScreen() {
    CoachDetailScreen(navController = rememberNavController())
}
