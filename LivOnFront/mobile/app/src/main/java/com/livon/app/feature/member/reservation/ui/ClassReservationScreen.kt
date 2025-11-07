package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.livon.app.R
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavHostController
import com.livon.app.ui.theme.Basic
import com.livon.app.ui.theme.BorderBlack
import com.livon.app.ui.theme.Unclickable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassReservationScreen(
    classes: List<SampleClassInfo>,
    onCardClick: (SampleClassInfo) -> Unit,
    onCoachClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialShowCalendar: Boolean = false,
    navController: NavHostController? = null
) {
    var showCalendar by remember { mutableStateOf(initialShowCalendar) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val filtered = remember(classes, selectedDate) {
        if (selectedDate == null) classes else classes.filter { it.date == selectedDate }
    }

    CommonScreenC(
        topBar = { TopBar(title = "예약하기", onBack = { navController?.popBackStack() ?: Unit }) },
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
                            onCardClick = {
                                if (navController != null) {
                                    try {
                                        navController.navigate("class_detail/${item.id}")
                                    } catch (_: Exception) {
                                        onCardClick(item)
                                    }
                                } else {
                                    onCardClick(item)
                                }
                            },
                            onCoachClick = { onCoachClick(item.coachId) }
                        )
                    }
                }
            }

            // 달력 모달(하단 시트) - ModalBottomSheet으로 변경: swipe-to-dismiss 지원
            if (showCalendar) {
                ModalBottomSheet(
                    onDismissRequest = { showCalendar = false },
                    tonalElevation = 8.dp,
                    containerColor = Basic ,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 8.dp)

                            )
                        }
                    }
                ) {
                    // Sheet content: header (예약 정보 centered), month nav, calendar (full width), confirm button
                    CalendarSheetContent(
                        selectedDate = selectedDate,
                        onSelect = { dateOrNull ->               // 🔧 CHANGED: (LocalDate?) 받도록
                            selectedDate = dateOrNull            // ← nullable 그대로 저장
                        },
                        onConfirm = {
                            showCalendar = false
                        }
                    )
                }
            }
        }
    }
}

/* -------------------- 시트 내부 (달력 + 하단 버튼) -------------------- */

@Composable
private fun CalendarSheetContent(
    selectedDate: LocalDate?,
    onSelect: (LocalDate?) -> Unit,
    onConfirm: () -> Unit
) {

    val H_MARGIN = 16.dp
    // 화면 높이에 따라 적절히 크기 조절 (최대 높이 지정)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Basic)
            .padding(top = 4.dp, start = 0.dp, end = 0.dp, bottom = 0.dp)
            .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Basic)
                .padding(horizontal = H_MARGIN), // 🔧 ADDED: 버튼과 동일 좌우 마진
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "예약 정보", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))

            // month navigation row
            var currentMonth by remember { mutableStateOf(selectedDate?.let { YearMonth.from(it) } ?: YearMonth.now()) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Basic)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Prev month"
                    )
                }

                Spacer(Modifier.width(50.dp))
                Text(text = "${currentMonth.monthValue}월 ${currentMonth.year}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.width(50.dp))
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Next month",
                        modifier = Modifier.graphicsLayer(
                            scaleX = -1f   // 좌우반전
                        )
                    )
                }

            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Basic)
                    .padding(horizontal = H_MARGIN)
                    .heightIn(min = 320.dp, max = 330.dp)
            ) {
                Surface(color = Basic,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CalendarMonth(
                        yearMonth = currentMonth,
                        selected = selectedDate,
                        onSelect = { date ->
                            if (selectedDate == date) {
                                onSelect(null)                 // ✅ 다시 누르면 취소
                            } else {
                                onSelect(date)
                                currentMonth = YearMonth.from(date)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            val isEnabled = selectedDate != null
            // Confirm button
            PrimaryButtonBottom(
                text = "선택",
                enabled = isEnabled,          // ← 날짜 없으면 자동 비활성
                onClick = onConfirm,
                bottomMargin = 0.dp,
                applyNavPadding = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Basic)
                    .padding( vertical = 8.dp)
            )
        }
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
