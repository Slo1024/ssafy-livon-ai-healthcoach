package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues
import com.livon.app.R
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.navbar.HomeNavBar
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.ZeroCornerSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassReservationScreen(
    classes: List<SampleClassInfo>,
    onCardClick: (SampleClassInfo) -> Unit,
    onCoachClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialShowCalendar: Boolean = false
) {
    val isPreview = LocalInspectionMode.current

    var showCalendar by remember { mutableStateOf(initialShowCalendar) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val filtered = remember(classes, selectedDate) {
        if (selectedDate == null) classes else classes.filter { it.date == selectedDate }
    }

    CommonScreenC(
        topBar = { TopBar(title = "예약하기", onBack = {}) },
        modifier = modifier
    ) {
        // 메인 레이아웃
        Box(Modifier.fillMaxSize()) {
            // 리스트 레이어
            Column(Modifier.fillMaxSize()) {
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
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selectedDate?.toString() ?: "날짜 선택",
                            fontSize = 16.sp
                        )
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

            // 달력 모달(스크림 + 하단 시트) - 프리뷰/런타임 동일 동작
            if (showCalendar) {
                // 스크림: 화면 어둡게, 탭하면 닫힘
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000))
                        .clickable { showCalendar = false }
                        .zIndex(1f) // 위에 표시
                )

                // 하단 시트: 화면 너비 꽉 채움, 양옆 마진 무시
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = Color.White
                    ) {
                        // 시트 내부: 달력(흰색 배경) + 하단 PrimaryButtonBottom("선택")
                        CalendarSheetContent(
                            selectedDate = selectedDate,
                            onSelect = { date -> selectedDate = date },
                            onConfirm = {
                                // confirm 선택시 동작: 시트 닫기 (필요시 추가 동작 연결)
                                showCalendar = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/* -------------------- 시트 내부 (달력 + 하단 버튼) -------------------- */

@Composable
private fun CalendarSheetContent(
    selectedDate: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    onConfirm: () -> Unit
) {
    // 화면 높이에 따라 적절히 크기 조절 (최대 높이 지정)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 0.dp, end = 0.dp, bottom = 0.dp)
            .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
    ) {
        // 헤더 (닫기 버튼 위치나 제목이 필요하면 추가 가능)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "예약 정보", style = MaterialTheme.typography.titleMedium)
            // 닫기 아이콘 대신 빈 영역 (스크림 탭으로 닫기 처리)
            Spacer(modifier = Modifier.width(24.dp))
        }

        // 달력 영역: 흰색 배경(상위 Surface가 흰색이므로 별도 색 불필요)
        val ym = selectedDate?.let { YearMonth.from(it) } ?: YearMonth.now()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp, max = 520.dp)
        ) {
            CalendarMonth(
                yearMonth = ym,
                selected = selectedDate,
                onSelect = { date -> onSelect(date) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(12.dp))

        // 하단 버튼: "선택" 텍스트, 내비게이션 바 없음
        PrimaryButtonBottom(
            text = "선택",
            enabled = true,
            onClick = onConfirm,
            bottomMargin = 0.dp,
            applyNavPadding = true, // 내비바가 없는 시트이므로 true/false는 필요에 따라 조정
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

/* -------------------- Preview -------------------- */

@Preview(showBackground = true, name = "예약 리스트")
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

@Preview(showBackground = true, name = "달력 시트(프리뷰 강제 오픈)")
@Composable
private fun ClassReservationScreenCalendarPreview() {
    val items = sampleItemsForPreview()
    LivonTheme {
        ClassReservationScreen(
            classes = items,
            onCardClick = {},
            onCoachClick = {},
            initialShowCalendar = true
        )
    }
}

/* --- 샘플 데이터 (프리뷰용) --- */
private fun sampleItemsForPreview() = listOf(
    SampleClassInfo(
        id = "1",
        coachId = "c1",
        date = LocalDate.now(),
        time = "11:00 ~ 12:00",
        type = "일반 클래스",
        imageUrl = null,
        className = "직장인을 위한 코어 강화",
        coachName = "김리본 코치",
        description = "점심시간 30분 집중 코어 운동.",
        currentParticipants = 7,
        maxParticipants = 10
    ),
    SampleClassInfo(
        id = "2",
        coachId = "c2",
        date = LocalDate.now().plusDays(1),
        time = "19:00 ~ 20:00",
        type = "기업 클래스",
        imageUrl = null,
        className = "퇴근 후 스트레칭",
        coachName = "박생존 코치",
        description = "힐링 스트레칭.",
        currentParticipants = 10,
        maxParticipants = 10
    )
)
