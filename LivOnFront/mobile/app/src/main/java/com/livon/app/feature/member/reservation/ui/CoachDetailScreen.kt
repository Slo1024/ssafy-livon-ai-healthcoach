package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.livon.app.feature.member.reservation.vm.CoachDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenA
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.theme.Basic
import java.time.LocalDate
import java.time.YearMonth
import androidx.navigation.NavHostController
import com.livon.app.ui.component.navbar.HomeNavBar
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import com.livon.app.feature.member.reservation.model.CoachUIModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

@Composable
fun CoachDetailScreen(
    coachId: String,
    onBack: () -> Unit,
    showSchedule: Boolean = true,
    navController: NavHostController? = null,
    viewModelFactory: androidx.lifecycle.ViewModelProvider.Factory? = null,
    onReserve: (coachName: String, date: LocalDate, time: String) -> Unit = { _, _, _ -> }
) {
    val vm = if (viewModelFactory != null) viewModel(factory = viewModelFactory) as CoachDetailViewModel
    else viewModel() as CoachDetailViewModel

    val state by vm.uiState.collectAsState()

    // [수정] showSchedule=true일 때만 날짜/시간 선택 관련 상태 및 API 호출 사용
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showTimeSelection by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    // [수정] showSchedule=true일 때만 예약 관련 상태 관리
    val reservedTimeTokensByDate = remember { mutableStateOf<Map<LocalDate, Set<String>>>(emptyMap()) }

    val ctx = LocalContext.current

    // [추가] 헬퍼 함수: "HH:MM-HH:MM" 형식을 "AM_H:00" 또는 "PM_H:00" 토큰으로 변환
    fun convertTimeRangeToToken(timeRange: String): String? {
        return try {
            val startTime = timeRange.substringBefore("-")
            val parts = startTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                if (hour in 0..23 && minute == 0) {
                    if (hour < 12) {
                        val h = if (hour == 0) 12 else hour
                        "AM_$h:00"
                    } else {
                        val hh = hour % 12
                        val h = if (hh == 0) 12 else hh
                        "PM_$h:00"
                    }
                } else null
            } else null
        } catch (_: Throwable) { null }
    }

    // [수정] showSchedule=true일 때만 예약 관련 API 호출 및 상태 관리
    if (showSchedule) {
        // Combine server-side upcoming reservations and local cache updates to compute reserved tokens.
        LaunchedEffect(coachId) {
            val repo = com.livon.app.data.repository.ReservationRepositoryImpl()
            try { repo.loadPersistedReservations(ctx) } catch (_: Throwable) {}
            
            // 현재 사용자의 예약 정보 수집
            val serverMap = mutableMapOf<LocalDate, MutableSet<String>>()
            try {
                val res = try { 
                    repo.getMyReservations(status = "upcoming", type = "ONE") 
                } catch (t: Throwable) { 
                    Result.failure<com.livon.app.data.remote.api.ReservationListResponse>(t) 
                }
                
                if (res.isSuccess) {
                    val body = res.getOrNull()
                    body?.items?.forEach { dto ->
                        try {
                            val coachUserId = dto.coach?.userId
                            if ((dto.type ?: "ONE") == "ONE" && coachUserId == coachId) {
                                val startIso = dto.startAt
                                if (!startIso.isNullOrBlank()) {
                                    val start = java.time.LocalDateTime.parse(startIso)
                                    val date = start.toLocalDate()
                                    val token = if (start.hour < 12) {
                                        val h = if (start.hour % 12 == 0) 12 else (start.hour % 12)
                                        "AM_${h}:00"
                                    } else {
                                        val hh = start.hour % 12
                                        val h = if (hh == 0) 12 else hh
                                        "PM_${h}:00"
                                    }
                                    serverMap.getOrPut(date) { mutableSetOf() }.add(token)
                                }
                            }
                        } catch (_: Throwable) { }
                    }
                }
            } catch (_: Throwable) { /* ignore server fetch errors */ }

            // localReservationsFlow 구독하여 예약 상태 실시간 업데이트
            try {
                com.livon.app.data.repository.ReservationRepositoryImpl.localReservationsFlow.collect { localList ->
                    val map = mutableMapOf<LocalDate, MutableSet<String>>()
                    
                    // 서버 예약 정보로 시작
                    serverMap.forEach { (d, tokens) -> map[d] = tokens.toMutableSet() }

                    // 로컬 캐시 예약 추가
                    localList.forEach { lr ->
                        try {
                            if (lr.type == com.livon.app.data.repository.ReservationType.PERSONAL && lr.coachId == coachId) {
                                val start = java.time.LocalDateTime.parse(lr.startAt)
                                val date = start.toLocalDate()
                                val token = if (start.hour < 12) {
                                    val h = if (start.hour % 12 == 0) 12 else (start.hour % 12)
                                    "AM_${h}:00"
                                } else {
                                    val hh = start.hour % 12
                                    val h = if (hh == 0) 12 else hh
                                    "PM_${h}:00"
                                }
                                map.getOrPut(date) { mutableSetOf() }.add(token)
                            }
                        } catch (_: Throwable) { }
                    }

                    // 이미 조회된 예약 불가능한 시간 병합
                    reservedTimeTokensByDate.value.forEach { (date, tokens) ->
                        map.getOrPut(date) { mutableSetOf() }.addAll(tokens)
                    }

                    // 최종 상태 업데이트
                    val finalMap = map.mapValues { it.value.toSet() }
                    reservedTimeTokensByDate.value = finalMap
                }
            } catch (_: Throwable) { /* ignore collection errors */ }
        }

        // [수정] 선택된 날짜가 변경될 때 해당 날짜의 예약 정보를 동적으로 로드
        LaunchedEffect(selectedDate, coachId) {
            if (selectedDate == null) {
                selectedTime = null
                showTimeSelection = false
            } else {
                selectedTime = null
                
                selectedDate?.let { sd ->
                    try {
                        val coachApi = com.livon.app.core.network.RetrofitProvider.createService(
                            com.livon.app.data.remote.api.CoachApiService::class.java
                        )
                        val dateStr = sd.toString()
                        
                        val currentMap = reservedTimeTokensByDate.value.toMutableMap()
                        val tokens = currentMap.getOrPut(sd) { mutableSetOf() }.toMutableSet()
                        
                        // 예약 가능한 시간 조회 (availableTimes API)
                        val availableTimesRes = try {
                            coachApi.getCoachAvailableTimes(coachId, date = dateStr)
                        } catch (t: Throwable) {
                            null
                        }
                        
                        if (availableTimesRes != null && availableTimesRes.isSuccess) {
                            val response = availableTimesRes.result
                            
                            // 전체 가능한 시간대에서 availableTimes를 제외하여 예약 불가능한 시간 계산
                            val allPossibleTimes = listOf(
                                "09:00-10:00", "10:00-11:00", "11:00-12:00", 
                                "12:00-13:00", "13:00-14:00", "14:00-15:00", 
                                "15:00-16:00", "16:00-17:00", "17:00-18:00"
                            )
                            
                            val availableSet = (response?.availableTimes ?: emptyList()).toSet()
                            val unavailableTimes = allPossibleTimes.filter { it !in availableSet }
                            
                            // 예약 불가능한 시간을 토큰으로 변환하여 추가
                            unavailableTimes.forEach { timeRange ->
                                convertTimeRangeToToken(timeRange)?.let { token ->
                                    tokens.add(token)
                                }
                            }
                        }
                        
                        // 코치가 막아놓은 시간대 조회 (blockedTimes API)
                        val blockedTimesRes = try {
                            coachApi.getBlockedTimes(date = dateStr)
                        } catch (t: Throwable) {
                            null
                        }
                        
                        if (blockedTimesRes != null && blockedTimesRes.isSuccess) {
                            val response = blockedTimesRes.result
                            response?.blockedTimes?.forEach { timeRange ->
                                convertTimeRangeToToken(timeRange)?.let { token ->
                                    tokens.add(token)
                                }
                            }
                        }
                        
                        // 상태 업데이트
                        if (tokens.isNotEmpty()) {
                            currentMap[sd] = tokens.toSet()
                            reservedTimeTokensByDate.value = currentMap
                        }
                    } catch (_: Throwable) { /* API 호출 실패 시 무시 */ }
                }
            }
        }
    }

    // 코치 데이터 로드
    LaunchedEffect(coachId) {
        vm.load(coachId)
    }

    // [수정] showSchedule에 따라 UI 분기
    if (showSchedule) {
        // 개인 상담 예약 화면 (CommonSignUpScreenA)
        CommonSignUpScreenA(
            topBar = { TopBar(title = "상세 정보", onBack = onBack) },
            bottomBar = {
                val isSelectPhase = !showTimeSelection
                val buttonText = if (isSelectPhase) "선택" else "예약 하기"
                val enabled = if (isSelectPhase) selectedDate != null else selectedTime != null

                PrimaryButtonBottom(
                    text = buttonText,
                    enabled = enabled,
                    onClick = {
                        state.coach?.let { coach ->
                            if (isSelectPhase) {
                                if (selectedDate != null) {
                                    showTimeSelection = true
                                    selectedTime = null
                                }
                            } else {
                                if (selectedDate != null && selectedTime != null) {
                                    onReserve(coach.name, selectedDate!!, selectedTime!!)
                                }
                            }
                        }
                    }
                )
            }
        ) {
            val scroll = rememberScrollState()

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(bottom = 8.dp)
            ) {
                state.coach?.let { coach ->
                    // [추출] 코치 정보 표시 부분을 별도 Composable로 분리
                    CoachInfoSection(coach = coach, showAvatarLarge = true)

                    Spacer(Modifier.height(30.dp))

                    // 날짜/시간 선택 UI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Basic)
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 월 네비게이터
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "〈",
                                    modifier = Modifier.clickable { currentMonth = currentMonth.minusMonths(1) }
                                )
                                Text(
                                    text = "${currentMonth.monthValue}월 ${currentMonth.year}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "〉",
                                    modifier = Modifier.clickable { currentMonth = currentMonth.plusMonths(1) }
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // 달력
                            CalendarMonth(
                                yearMonth = currentMonth,
                                selected = selectedDate,
                                onSelect = { date ->
                                    selectedDate = if (selectedDate == date) null else date
                                    selectedTime = null
                                    showTimeSelection = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(12.dp))

                            // 시간 선택 UI
                            if (showTimeSelection && selectedDate != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(text = "오전", style = MaterialTheme.typography.titleSmall)

                                val allowedAmHours = listOf(9, 10, 11)
                                val allowedPmHours = listOf(12, 1, 2, 3, 4, 5)

                                val amItems = allowedAmHours.map { hour -> Pair("${hour}:00", "AM_${hour}:00") }
                                val pmItems = allowedPmHours.map { hour -> Pair("${hour}:00", "PM_${hour}:00") }

                                val disabledForSelected = selectedDate?.let { reservedTimeTokensByDate.value[it] } ?: emptySet()
                                
                                TimeGrid(
                                    timeItems = amItems,
                                    selected = selectedTime,
                                    onSelect = { t -> selectedTime = t },
                                    disabledTokens = disabledForSelected
                                )

                                Spacer(Modifier.height(8.dp))
                                Text(text = "오후", style = MaterialTheme.typography.titleSmall)

                                TimeGrid(
                                    timeItems = pmItems,
                                    selected = selectedTime,
                                    onSelect = { t -> selectedTime = t },
                                    disabledTokens = disabledForSelected
                                )

                                Spacer(Modifier.height(40.dp))
                            }

                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    } else {
        // 코치 정보만 표시하는 화면 - ClassDetailScreen과 동일한 레이아웃 구조
        // [수정] Scaffold + TopBar + HomeNavBar 구조로 변경 (ClassDetailScreen과 동일)
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                // 상태바 안전영역
                Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                    TopBar(title = "상세 정보", onBack = onBack)
                }
            },
            bottomBar = {
                // [수정] ClassDetailScreen과 동일한 하단 바 배치
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
                ) {
                    HomeNavBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        currentRoute = null,
                        navController = navController,
                        onNavigate = { route ->
                            when (route) {
                                "home" -> navController?.navigate(com.livon.app.navigation.Routes.MemberHome)
                                "booking" -> navController?.navigate(com.livon.app.navigation.Routes.ReservationModeSelect)
                                "reservations" -> navController?.navigate(com.livon.app.navigation.Routes.Reservations)
                                "mypage" -> navController?.navigate(com.livon.app.navigation.Routes.MyPage)
                                else -> {}
                            }
                        }
                    )
                }
            }
        ) { inner ->
            // [수정] ClassDetailScreen과 동일한 스크롤 가능한 레이아웃
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                state.coach?.let { coach ->
                    // [수정] showSchedule=true와 동일한 프로필 디자인 재사용 (showAvatarLarge=true)
                    CoachInfoSection(coach = coach, showAvatarLarge = true)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * [추출] 코치 정보 표시 Composable - showSchedule true/false 모두에서 재사용
 */
@Composable
private fun CoachInfoSection(
    coach: CoachUIModel,
    showAvatarLarge: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val avatarPainter = rememberAsyncImagePainter(coach.avatarUrl ?: "")
        
        Image(
            painter = avatarPainter,
            contentDescription = "coach",
            modifier = Modifier
                .size(if (showAvatarLarge) 150.dp else 100.dp)
                .clip(CircleShape)
                .then(
                    if (!showAvatarLarge) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(if (showAvatarLarge) 30.dp else 12.dp))

        Text(
            text = coach.name,
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(Modifier.height(if (showAvatarLarge) 12.dp else 8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (showAvatarLarge) 12.dp else 0.dp)
                .then(if (!showAvatarLarge) Modifier.padding(top = 8.dp) else Modifier)
        ) {
            Text(
                text = "코치 소개",
                style = if (showAvatarLarge) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.height(if (showAvatarLarge) 20.dp else 8.dp))
            
            Text(
                text = "직무",
                style = if (showAvatarLarge) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall
            )
            Text(
                text = coach.job ?: "",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(if (showAvatarLarge) 16.dp else 8.dp))
            
            Text(
                text = "자격증",
                style = if (showAvatarLarge) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall
            )
            coach.certificates.forEach { cert: String ->
                Text(text = cert, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(if (showAvatarLarge) 16.dp else 8.dp))
            
            Text(
                text = "소개",
                style = if (showAvatarLarge) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall
            )
            Text(
                text = coach.intro,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 시간 선택 그리드 Composable
 */
@Composable
private fun TimeGrid(
    timeItems: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String?) -> Unit,
    disabledTokens: Set<String> = emptySet()
) {
    Column {
        timeItems.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { (display, value) ->
                    val isSelected = value == selected
                    val isDisabled = disabledTokens.contains(value)
                    OutlinedButton(
                        onClick = { 
                            if (!isDisabled) { 
                                if (isSelected) onSelect(null) else onSelect(value) 
                            } 
                        },
                        enabled = !isDisabled,
                        modifier = Modifier.size(width = 100.dp, height = 35.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            0.8.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) 
                            else MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Text(
                            text = display,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else if (isDisabled) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) 
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}