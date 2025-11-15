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
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.theme.Basic
import java.time.LocalDate
import java.time.YearMonth
import androidx.navigation.NavHostController
import com.livon.app.ui.component.navbar.HomeNavBar
import androidx.compose.ui.platform.LocalContext

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

    // Shared selection state so bottomBar (PrimaryButtonBottom) can react to selections
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showTimeSelection by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    // Placeholder availability data structures (can be populated from API later)
    val placeholderAvailableDates = remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    val placeholderAvailableTimesByDate = remember { mutableStateOf<Map<LocalDate, List<String>>>(emptyMap()) }

    // reservedTimeTokensByDate: map date -> set of time tokens (e.g., "AM_9:00") reserved on that date
    val reservedTimeTokensByDate = remember { mutableStateOf<Map<LocalDate, Set<String>>>(emptyMap()) }
    // keep a union set for fallback or previews
    val reservedTimeTokensAll = remember { mutableStateOf<Set<String>>(emptySet()) }

    val ctx = LocalContext.current
    // [추가] 헬퍼 함수: "HH:MM-HH:MM" 형식을 "AM_H:00" 또는 "PM_H:00" 토큰으로 변환
    // Composable 함수 최상위 레벨에 정의하여 모든 LaunchedEffect 블록에서 접근 가능하도록 함
    fun convertTimeRangeToToken(timeRange: String): String? {
        return try {
            val startTime = timeRange.substringBefore("-") // "09:00-10:00" -> "09:00"
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

    // Combine server-side upcoming reservations and local cache updates to compute reserved tokens.
    // [수정] 코치의 모든 예약된 시간을 가져와서 비활성화 처리 (다른 사용자가 예약한 경우 포함)
    LaunchedEffect(coachId) {
        val repo = com.livon.app.data.repository.ReservationRepositoryImpl()
        try { repo.loadPersistedReservations(ctx) } catch (_: Throwable) {}
        
        // [수정] 초기 로드 시에는 전체 범위를 호출하지 않고, 필요한 날짜만 호출하도록 변경
        // 대신 선택된 날짜가 변경될 때 해당 날짜만 조회하도록 함 (LaunchedEffect(selectedDate)에서 처리)
        // 여기서는 현재 사용자의 예약 정보만 수집
        
        // Fetch server-side upcoming reservations once and build a map of date->tokens
        // [수정] 현재 사용자의 예약도 포함하여 병합
        val serverMap = mutableMapOf<LocalDate, MutableSet<String>>()
        try {
            val res = try { repo.getMyReservations(status = "upcoming", type = null) } catch (t: Throwable) { Result.failure<com.livon.app.data.remote.api.ReservationListResponse>(t) }
            if (res.isSuccess) {
                val body = res.getOrNull()
                body?.items?.forEach { dto ->
                    try {
                        val coachUserId = dto.coach?.userId
                        // [수정] 현재 사용자의 예약만이 아니라, 해당 코치의 모든 예약을 확인해야 하지만
                        // getMyReservations는 현재 사용자의 예약만 반환하므로, 
                        // 다른 사용자의 예약은 getCoachAvailableTimes API를 통해 확인 필요
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
        
        // [수정] 초기 로드 시에는 선택된 날짜에 대한 API 호출을 하지 않음
        // 선택된 날짜가 변경될 때만 해당 날짜의 예약 불가능한 시간을 조회하도록 함

        // [수정] collect local cache updates and merge with the serverMap into a date->tokens map
        // 예약 취소 시 자동으로 갱신되도록 localReservationsFlow를 구독
        try {
            com.livon.app.data.repository.ReservationRepositoryImpl.localReservationsFlow.collect { localList: List<com.livon.app.data.repository.ReservationRepositoryImpl.LocalReservation> ->
                val map = mutableMapOf<LocalDate, MutableSet<String>>()
                
                // start with server map (현재 사용자 예약)
                serverMap.forEach { (d, tokens) -> map[d] = tokens.toMutableSet() }

                // add local reservations into map (최근 생성된 예약 포함, 취소된 예약은 제외됨)
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

                // [추가] reservedTimeTokensByDate에 이미 있는 데이터도 병합 (선택된 날짜에 대해 조회된 예약 불가능한 시간 포함)
                reservedTimeTokensByDate.value.forEach { (date, tokens) ->
                    map.getOrPut(date) { mutableSetOf() }.addAll(tokens)
                }

                // finalize map and union
                val finalMap = map.mapValues { it.value.toSet() }
                reservedTimeTokensByDate.value = finalMap
                reservedTimeTokensAll.value = finalMap.values.flatten().toSet()
            }
        } catch (_: Throwable) { /* ignore collection errors */ }
    }

    // [수정] 선택된 날짜가 변경될 때 해당 날짜의 예약 정보를 동적으로 로드
    LaunchedEffect(selectedDate, coachId) {
        if (selectedDate == null) {
            selectedTime = null
            showTimeSelection = false
        } else {
            // if date changed by user selection, clear any previously chosen time
            selectedTime = null
            
            // [추가] 선택된 날짜의 예약 정보를 동적으로 로드
            // [수정] selectedDate를 먼저 null-check하고 로컬 변수로 캡처하여 smart-cast 가능하도록 함
            selectedDate?.let { sd ->
                try {
                    val coachApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.CoachApiService::class.java)
                    val dateStr = sd.toString() // YYYY-MM-DD 형식
                    
                    val currentMap = reservedTimeTokensByDate.value.toMutableMap()
                    val tokens = currentMap.getOrPut(sd) { mutableSetOf() }.toMutableSet()
                    
                    // 예약된 시간 조회
                    val availableTimesRes = try {
                        coachApi.getCoachAvailableTimes(coachId, date = dateStr)
                    } catch (t: Throwable) {
                        // API가 아직 구현되지 않았을 수 있으므로 실패해도 계속 진행
                        null
                    }
                    
                    if (availableTimesRes != null && availableTimesRes.isSuccess) {
                        val response = availableTimesRes.result
                        
                        // [수정] API는 availableTimes(예약 가능한 시간)를 반환합니다
                        // 전체 가능한 시간대 (09:00~17:00)에서 availableTimes를 제외하여 예약 불가능한 시간을 계산
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
                    
                    // [추가] 코치가 막아놓은 시간대 조회
                    val blockedTimesRes = try {
                        coachApi.getBlockedTimes(date = dateStr)
                    } catch (t: Throwable) {
                        // API가 아직 구현되지 않았을 수 있으므로 실패해도 계속 진행
                        null
                    }
                    
                    if (blockedTimesRes != null && blockedTimesRes.isSuccess) {
                        val response = blockedTimesRes.result
                        response?.blockedTimes?.forEach { timeRange ->
                            // "09:00-10:00" 형식을 "AM_9:00" 같은 토큰으로 변환
                            convertTimeRangeToToken(timeRange)?.let { token ->
                                tokens.add(token)
                            }
                        }
                    }
                    
                    // 한 번만 상태 업데이트
                    if (tokens.isNotEmpty()) {
                        currentMap[sd] = tokens.toSet()
                        reservedTimeTokensByDate.value = currentMap
                    }
                } catch (_: Throwable) { /* API 호출 실패 시 무시 */ }
            }
        }
    }

    if (showSchedule) {
        // Use CommonSignUpScreenA to ensure consistent top/bottom bars and navigation padding.
        CommonSignUpScreenA(
            topBar = { TopBar(title = "상세 정보", onBack = onBack) },
            bottomBar = {
                // Single persistent bottom button whose text and enabled state depend on selection state
                val isSelectPhase = !showTimeSelection
                val buttonText = if (isSelectPhase) "선택" else "예약 하기"
                val enabled = if (isSelectPhase) selectedDate != null else selectedTime != null

                PrimaryButtonBottom(
                    text = buttonText,
                    enabled = enabled,
                    onClick = {
                        state.coach?.let { coach ->
                            if (isSelectPhase) {
                                // move from date-selection phase to time selection
                                if (selectedDate != null) {
                                    // when entering time selection, load available times for that date from ViewModel (if implemented)
                                    // TODO: vm.loadAvailableTimes(coachId, selectedDate!!)
                                    showTimeSelection = true
                                    selectedTime = null
                                }
                            } else {
                                // reservation phase
                                if (selectedDate != null && selectedTime != null) {
                                    onReserve(coach.name, selectedDate!!, selectedTime!!)
                                }
                            }
                        }
                    }
                )
            }
        ) {
            // content area: make scrollable
            val scroll = rememberScrollState()

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(bottom = 8.dp)
            ) {
                state.coach?.let { coach ->
                    Spacer(Modifier.height(24.dp))

                    val avatarPainter = rememberAsyncImagePainter(coach.avatarUrl ?: "")
                    Image(
                        painter = avatarPainter,
                        contentDescription = "coach",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(30.dp))

                    Text(text = coach.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(12.dp))

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Text(text = "코치 소개", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(20.dp))
                        Text(text = "직무", style = MaterialTheme.typography.titleMedium)
                        Text(text = coach.job ?: "", style = MaterialTheme.typography.bodyMedium)

                        Spacer(Modifier.height(16.dp))
                        Text(text = "자격증", style = MaterialTheme.typography.titleMedium)
                        coach.certificates.forEach { cert ->
                            Text(text = cert, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(text = "소개", style = MaterialTheme.typography.titleMedium)
                        Text(text = coach.intro, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(30.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Basic)
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Month navigator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "〈", modifier = Modifier.clickable { currentMonth = currentMonth.minusMonths(1) })
                                Text(text = "${currentMonth.monthValue}월 ${currentMonth.year}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "〉", modifier = Modifier.clickable { currentMonth = currentMonth.plusMonths(1) })
                            }

                            Spacer(Modifier.height(8.dp))

                            // Wrap CalendarMonth: prevent selection when date not available
                            CalendarMonth(
                                yearMonth = currentMonth,
                                selected = selectedDate,
                                onSelect = { date ->
                                    // Only allow selecting if date is available (placeholder set empty -> allow all)
                                    val allowed = placeholderAvailableDates.value.ifEmpty { null }?.contains(date) ?: true
                                    if (allowed) {
                                        selectedDate = if (selectedDate == date) null else date
                                        selectedTime = null
                                        showTimeSelection = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(12.dp))

                            // Note: the bottom persistent button handles moving from date->time phase and final reservation.

                            if (showTimeSelection) {
                                Spacer(Modifier.height(8.dp))
                                Text(text = "오전", style = MaterialTheme.typography.titleSmall)

                                // Build display/value pairs limited to allowed personal consultation hours: 09:00~17:00
                                val allowedAmHours = listOf(9, 10, 11)
                                val allowedPmHours = listOf(12, 1, 2, 3, 4, 5) // 12 -> 12:00 (noon), 1..5 -> 13:00..17:00

                                val amItems = allowedAmHours.map { hour -> Pair("${hour}:00", "AM_${hour}:00") }
                                val pmItems = allowedPmHours.map { hour -> Pair("${hour}:00", "PM_${hour}:00") }

                                // If available times for the selected date exist in placeholder map, filter to allowed hours, otherwise show allowed lists
                                val availableForDate = selectedDate?.let { d -> placeholderAvailableTimesByDate.value[d] }

                                val amFiltered = availableForDate
                                    ?.filter { it.startsWith("AM_") }
                                    ?.mapNotNull { valStr ->
                                        val hour = valStr.removePrefix("AM_").substringBefore(":").toIntOrNull()
                                        if (hour != null && allowedAmHours.contains(hour)) Pair(valStr.removePrefix("AM_"), valStr) else null
                                    } ?: amItems

                                val disabledForSelected = selectedDate?.let { reservedTimeTokensByDate.value[it] } ?: emptySet()
                                TimeGrid(timeItems = amFiltered, selected = selectedTime, onSelect = { t -> selectedTime = t }, disabledTokens = disabledForSelected)

                                Spacer(Modifier.height(8.dp))
                                Text(text = "오후", style = MaterialTheme.typography.titleSmall)

                                val pmFiltered = availableForDate
                                    ?.filter { it.startsWith("PM_") }
                                    ?.mapNotNull { valStr ->
                                        val hour = valStr.removePrefix("PM_").substringBefore(":").toIntOrNull()
                                        if (hour != null && allowedPmHours.contains(hour)) Pair(valStr.removePrefix("PM_"), valStr) else null
                                    } ?: pmItems

                                TimeGrid(timeItems = pmFiltered, selected = selectedTime, onSelect = { t -> selectedTime = t }, disabledTokens = disabledForSelected)

                                Spacer(Modifier.height(40.dp)) // extra spacing so content not hidden by bottom bar
                            }

                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    // content end
                }
            }

            // Because CommonSignUpScreenA reserves space for bottomBar, content includes padding to avoid overlap.
        }
    } else {
        // Group consultation: use CommonScreenC so layout/padding matches other non-signup screens
        CommonScreenC(
            topBar = { TopBar(title = "상세 정보", onBack = onBack) },
            content = {
                Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
                    state.coach?.let { coach ->
                        val avatarPainter = rememberAsyncImagePainter(coach.avatarUrl ?: "")
                        Image(
                            painter = avatarPainter,
                            contentDescription = "coach",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(12.dp))
                        Text(text = coach.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(Modifier.height(8.dp))

                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text(text = "코치 소개", style = MaterialTheme.typography.titleSmall)
                            Text(text = coach.job ?: "", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))

                            Text(text = "자격증", style = MaterialTheme.typography.titleSmall)
                            coach.certificates.forEach { cert ->
                                Text(text = cert, style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(text = "소개", style = MaterialTheme.typography.titleSmall)
                            Text(text = coach.intro, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            fullBleedContent = {
                // Place HomeNavBar in full-bleed area so it's pinned at bottom similar to QnASubmitScreen / MemberHomeRoute
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
        )
    }

    LaunchedEffect(coachId) {
        vm.load(coachId)
        // TODO: if your ViewModel provides methods to load availability, call them here
        // e.g. vm.loadAvailableDates(coachId)
    }
}

@Composable
private fun TimeGrid(timeItems: List<Pair<String, String>>, selected: String?, onSelect: (String?) -> Unit, disabledTokens: Set<String> = emptySet()) {
    Column {
        timeItems.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { (display, value) ->
                    val isSelected = value == selected
                    val isDisabled = disabledTokens.contains(value)
                    OutlinedButton(
                        onClick = { if (!isDisabled) { if (isSelected) onSelect(null) else onSelect(value) } },
                        enabled = !isDisabled,
                        modifier = Modifier.size(width = 100.dp, height = 35.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Text(text = display, style = MaterialTheme.typography.bodySmall, color = if (isSelected) MaterialTheme.colorScheme.primary else if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
