package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.R
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate
import java.time.YearMonth
import com.livon.app.ui.component.calendar.MonthNavigator
import com.livon.app.ui.component.calendar.CalendarMonth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassReservationScreen(
    onBack: () -> Unit,
    onNavigateToClassDetail: (classId: String) -> Unit,
    onNavigateToCoachDetail: (coachId: String) -> Unit,
) {
    // 샘플 데이터(프리뷰/임시) — 실제에선 ViewModel에서 주입
    val classes = remember {
        listOf(
            SampleClassInfo(
                id = "c1",
                coachId = "k1",
                date = LocalDate.now(),
                time = "09:00",
                type = "일반 클래스",
                imageUrl = null,
                className = "아침 러닝",
                coachName = "김코치",
                description = "인터벌 러닝으로 컨디션 업!",
                currentParticipants = 3,
                maxParticipants = 10
            ),
            SampleClassInfo(
                id = "c2",
                coachId = "k2",
                date = LocalDate.now().plusDays(1),
                time = "12:00",
                type = "기업 클래스",
                imageUrl = null,
                className = "점심 필라테스",
                coachName = "박코치",
                description = "점심시간 30분 코어 강화",
                currentParticipants = 10,
                maxParticipants = 10
            ),
            SampleClassInfo(
                id = "c3",
                coachId = "k3",
                date = LocalDate.now(),
                time = "19:00",
                type = "일반 클래스",
                imageUrl = null,
                className = "저녁 PT",
                coachName = "이코치",
                description = "자세 교정 + 근력 보강",
                currentParticipants = 7,
                maxParticipants = 8
            ),
        )
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showCalendar by remember { mutableStateOf(false) }

    // 날짜 필터
    val filtered = remember(classes, selectedDate) {
        if (selectedDate == null) classes else classes.filter { it.date == selectedDate }
    }

    CommonScreenC(
        topBar = { modifier ->
            TopBar(title = "클래스 예약", onBack = onBack, modifier = modifier)
        },
        content = {
            // ✅ CoachListScreen의 드롭다운 룩&필을 그대로 적용한 DateFilterDropdown
            DateFilterDropdown(
                selectedDate = selectedDate,
                onClick = { showCalendar = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 클래스 리스트
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { item ->
                    ClassCard(
                        classInfo = item,
                        onCardClick = { onNavigateToClassDetail(item.id) },
                        onCoachClick = { onNavigateToCoachDetail(item.coachId) }
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("해당 날짜에 예약 가능한 클래스가 없어요.")
                        }
                    }
                }
            }
        }
    )

    // 달력 BottomSheet
    if (showCalendar) {
        var currentYm by remember { mutableStateOf(YearMonth.from(selectedDate ?: LocalDate.now())) }
        var tempSelected by remember { mutableStateOf(selectedDate ?: LocalDate.now()) }

        ModalBottomSheet(
            onDismissRequest = { showCalendar = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                MonthNavigator(
                    yearMonth = currentYm,
                    onPrev = { currentYm = currentYm.minusMonths(1) },
                    onNext = { currentYm = currentYm.plusMonths(1) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CalendarMonth(
                    yearMonth = currentYm,
                    selected = tempSelected,
                    onSelect = { tempSelected = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCalendar = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            selectedDate = tempSelected
                            showCalendar = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("선택")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** -------------------------------------------------------------------
 *  CoachListScreen 스타일을 재사용한 날짜 드롭다운 (아이콘 + 텍스트)
 *  - 좌측: ic_calendar + "날짜 검색"(미선택) / 선택된 날짜 문자열
 *  - 우측: ArrowDropDown 아이콘
 *  - 배경: surfaceVariant, 라운드, 288:30 비율(가로 꽉참)
 *  - 클릭 시 onClick() → BottomSheet로 달력 오픈 (DropdownMenu 사용 X)
 * ------------------------------------------------------------------- */
@Composable
fun DateFilterDropdown(
    selectedDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // CoachTypeDropdown과 동일한 컨테이너 스타일
    Box(
        modifier = modifier
            .aspectRatio(288f / 30f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 달력 아이콘 + 텍스트
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = "calendar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = selectedDate?.toString() ?: "날짜 검색",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // 오른쪽: 드롭다운 화살표
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* -------------------------- Preview -------------------------- */

@Preview(showBackground = true, showSystemUi = true, name = "1) 드롭다운 클릭 전 (미선택)")
@Composable
private fun DateFilterDropdownPreview_Default() {
    LivonTheme {
        Column(Modifier.padding(16.dp)) {
            DateFilterDropdown(
                selectedDate = null,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2) 드롭다운 클릭 전 (선택된 날짜 표시)")
@Composable
private fun DateFilterDropdownPreview_Selected() {
    LivonTheme {
        Column(Modifier.padding(16.dp)) {
            DateFilterDropdown(
                selectedDate = LocalDate.of(2025, 11, 28),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "3) 날짜 선택 화면 (BottomSheet 열린 상태)")
@Composable
private fun ClassReservationScreenPreview_WithCalendar() {
    LivonTheme {
        // 미리 보기용으로 외부 상태를 흉내내려면 별도 래퍼를 만들어 사용하는 게 깔끔
        var open by remember { mutableStateOf(true) }
        val dummy: () -> Unit = {}
        // 실제 화면처럼 구성 (달력 띄운 상태를 시뮬레이션)
        ClassReservationScreen(
            onBack = dummy,
            onNavigateToClassDetail = { _ -> },
            onNavigateToCoachDetail = { _ -> }
        )
        // 주의: 실제 BottomSheet는 내부 상태로 열리므로 프리뷰에서 강제 오픈 제어는 제한적임
        // 위 ScreenPreview는 전체 흐름 확인용. (필요시 별도 샘플 Screen을 만들어 state 주입해도 됨)
    }
}
