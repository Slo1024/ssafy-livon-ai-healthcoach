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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.livon.app.R
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.Basic
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet


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

    // Determine which classes the current user already reserved (by consultationId)
    val reservedClassIds = remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(Unit) {
        try {
            val repo = com.livon.app.data.repository.ReservationRepositoryImpl()
            val res = try { repo.getMyReservations(status = "upcoming", type = null) } catch (t: Throwable) { Result.failure<com.livon.app.data.remote.api.ReservationListResponse>(t) }
            if (res.isSuccess) {
                val body = res.getOrNull()
                val ids = body?.items?.map { it.consultationId.toString() }?.toSet() ?: emptySet()
                reservedClassIds.value = ids
            }
        } catch (t: Throwable) { android.util.Log.w("ClassReservationScreen", "failed to load my reservations", t) }
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
                        val isClosed = item.currentParticipants >= item.maxParticipants
                        val isUserReserved = reservedClassIds.value.contains(item.id)
                        ClassCard(
                            classInfo = item,
                            onCardClick = {
                                if (isClosed || isUserReserved) {
                                    try { android.util.Log.d("ClassReservationScreen", "class ${item.id} is full or already reserved by user; navigation blocked") } catch (t: Throwable) { android.util.Log.w("ClassReservationScreen","log failed", t) }
                                    // no-op if full or already reserved
                                } else {
                                    if (navController != null) {
                                        try {
                                            navController.navigate("class_detail/${item.id}")
                                        } catch (_: Exception) {
                                            onCardClick(item)
                                        }
                                    } else {
                                        onCardClick(item)
                                    }
                                }
                            },
                            onCoachClick = {
                                // Debug logging: show submitted coachId and guard empty ids
                                try {
                                    android.util.Log.d("ClassReservationScreen", "CoachView clicked: coachId=${item.coachId}")
                                } catch (t: Throwable) { android.util.Log.w("ClassReservationScreen","log failed", t) }
                                val coachIdArg = if (item.coachId.isNullOrBlank()) "" else item.coachId
                                if (coachIdArg.isBlank()) android.util.Log.w("ClassReservationScreen", "coachId is blank; cannot navigate to coach detail")
                                onCoachClick(coachIdArg)
                            }
                            , enabled = !isClosed && !isUserReserved
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
                                    .size(width = 36.dp, height = 4.dp)

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Basic) // 최상위 배경을 Basic으로 고정
            .padding(top = 4.dp)
            .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Basic)
                .padding(horizontal = H_MARGIN),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "예약 정보", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))

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
                    Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Prev month")
                }

                Spacer(Modifier.width(50.dp))
                Text(text = "${currentMonth.monthValue}월 ${currentMonth.year}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.width(50.dp))
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Next month",
                        modifier = Modifier.graphicsLayer(scaleX = -1f)
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
                Surface(color = Basic, modifier = Modifier.fillMaxSize()) { // Surface도 Basic
                    CalendarMonth(
                        yearMonth = currentMonth,
                        selected = selectedDate,
                        onSelect = { date ->
                            if (selectedDate == date) {
                                onSelect(null)
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
            PrimaryButtonBottom(
                text = "선택",
                enabled = isEnabled,
                onClick = onConfirm,
                bottomMargin = 0.dp,
                applyNavPadding = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Basic) // 버튼 주변 배경도 Basic
                    .padding(vertical = 8.dp)
            )
        }
    }
}
