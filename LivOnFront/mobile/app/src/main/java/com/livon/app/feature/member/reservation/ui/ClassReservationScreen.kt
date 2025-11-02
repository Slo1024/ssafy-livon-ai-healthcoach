package com.livon.app.feature.member.reservation.ui

import com.livon.app.feature.member.reservation.ui.ClassCard
import com.livon.app.feature.member.reservation.ui.SampleClassInfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.navbar.HomeNavBar
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate
import java.time.YearMonth
import com.livon.app.ui.component.calendar.CalendarMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassReservationScreen(
    classes: List<SampleClassInfo>,
    onCardClick: (SampleClassInfo) -> Unit,
    onCoachClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialShowCalendar: Boolean = false // 프리뷰에서 시트를 열기 위한 플래그
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showCalendar by remember { mutableStateOf(initialShowCalendar) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // showCalendar 변경 시 sheetState를 show/hide하도록 처리 (suspend 함수 호출 가능)
    LaunchedEffect(showCalendar) {
        if (showCalendar) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    val filtered = remember(classes, selectedDate) {
        if (selectedDate == null) classes else classes.filter { it.date == selectedDate }
    }

    CommonScreenC(
        topBar = { TopBar(title = "예약하기", onBack = {}) },
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showCalendar = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null
                    )

                    Text(text = selectedDate?.toString() ?: "날짜 선택")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    ClassCard(
                        classInfo = item,
                        onCardClick = { onCardClick(item) },
                        onCoachClick = { onCoachClick(item.coachId) }
                    )
                }
            }
        }

        if (showCalendar) {
            ModalBottomSheet(
                onDismissRequest = { showCalendar = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
                        .heightIn(min = 200.dp, max = 600.dp)
                ) {
                    // 헤더
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("날짜 선택", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { selectedDate = null }) {
                            Text("초기화")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // CalendarMonth 시그니처에 맞게 호출
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val ym = selectedDate?.let { YearMonth.from(it) } ?: YearMonth.now()
                        CalendarMonth(
                            yearMonth = ym,
                            selected = selectedDate,
                            onSelect = { date: LocalDate ->
                                selectedDate = date
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 확인 / 닫기 버튼 영역
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCalendar = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("취소")
                        }

                        Button(
                            onClick = {
                                // 선택 완료 처리 (필요 시 콜백 호출)
                                showCalendar = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("확인")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 하단 버튼(PrimaryButtonBottom)과 홈 네비바를 함께 배치
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PrimaryButtonBottom(
                            text = "예약 확정하기",
                            enabled = true,
                            onClick = { /* 예약 확정 로직 */ }
                        )

                        Spacer(Modifier.height(6.dp))

                        HomeNavBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            currentRoute = null,
                            onNavigate = { route ->
                                // 네비게이션 처리
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "예약 리스트 미리보기")
@Composable
private fun ClassReservationScreenPreview() {
    val items = sampleItemsForPreview()
    LivonTheme {
        ClassReservationScreen(
            classes = items,
            onCardClick = {},
            onCoachClick = {}
        )
    }
}

@Preview(showBackground = true, name = "단일 카드 미리보기")
@Composable
private fun ClassReservationScreenSinglePreview() {
    val item = sampleItemsForPreview().first()
    LivonTheme {
        ClassReservationScreen(
            classes = listOf(item),
            onCardClick = {},
            onCoachClick = {}
        )
    }
}

@Preview(showBackground = true, name = "달력 시트 미리보기")
@Composable
private fun ClassReservationScreenCalendarPreview() {
    val items = sampleItemsForPreview()
    LivonTheme {
        // initialShowCalendar = true 로 달력(하단 시트)을 바로 표시
        ClassReservationScreen(
            classes = items,
            onCardClick = {},
            onCoachClick = {},
            initialShowCalendar = true
        )
    }
}

/* --- 테스트용 샘플 데이터 생성 (프리뷰용) --- */
private fun sampleItemsForPreview() = listOf(
    SampleClassInfo(
        id = "1",
        coachId = "c1",
        date = LocalDate.of(2025, 11, 28),
        time = "14:00",
        type = "일반 클래스",
        imageUrl = null,
        className = "직장인을 위한 코어 강화",
        coachName = "김리본 코치",
        description = "점심시간을 활용한 30분 집중 코어 운동 클래스입니다.",
        currentParticipants = 7,
        maxParticipants = 10
    ),
    SampleClassInfo(
        id = "2",
        coachId = "c2",
        date = LocalDate.of(2025, 11, 29),
        time = "19:00",
        type = "기업 클래스",
        imageUrl = null,
        className = "퇴근 후 스트레칭",
        coachName = "박생존 코치",
        description = "하루의 피로를 풀어주는 힐링 스트레칭 시간입니다.",
        currentParticipants = 10,
        maxParticipants = 10
    ),
    SampleClassInfo(
        id = "3",
        coachId = "c3",
        date = LocalDate.of(2025, 11, 30),
        time = "12:30",
        type = "일반 클래스",
        imageUrl = null,
        className = "오피스 요가",
        coachName = "이유나 코치",
        description = "짧고 상쾌한 오피스 요가 세션.",
        currentParticipants = 3,
        maxParticipants = 8
    )
)
