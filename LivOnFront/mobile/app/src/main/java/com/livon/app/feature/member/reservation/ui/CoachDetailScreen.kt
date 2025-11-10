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
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.calendar.CalendarMonth
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.theme.Basic
import java.time.LocalDate
import java.time.YearMonth
import androidx.navigation.NavHostController
import com.livon.app.ui.component.navbar.HomeNavBar

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

    // Placeholder availability data structures
    // TODO: Replace these with real data coming from ViewModel / API.
    // Example: vm.loadAvailableDates(coachId) -> sets vm.uiState.availableDates: Set<LocalDate>
    //          vm.loadAvailableTimes(coachId, date) -> vm.uiState.availableTimesByDate[date] : List<String> (values with AM_/PM_ prefix)
    val placeholderAvailableDates = remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    val placeholderAvailableTimesByDate = remember { mutableStateOf<Map<LocalDate, List<String>>>(emptyMap()) }

    // ensure mutual exclusivity: when date changes, clear selectedTime and hide time selection
    LaunchedEffect(selectedDate) {
        if (selectedDate == null) {
            selectedTime = null
            showTimeSelection = false
        } else {
            // if date changed by user selection, clear any previously chosen time
            selectedTime = null
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
//                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(text = coach.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(12.dp))

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        Text(text = "코치 소개", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Text(text = "직무", style = MaterialTheme.typography.titleLarge)
                        Text(text = coach.job ?: "", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        Text(text = "자격증", style = MaterialTheme.typography.titleLarge)
                        coach.certificates.forEach { cert ->
                            Text(text = cert, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(text = "소개", style = MaterialTheme.typography.titleLarge)
                        Text(text = coach.intro, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(24.dp))

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

                                // Build display/value pairs where value includes AM/PM to ensure uniqueness
                                val amItems = (1..12).map { hour -> Pair("${hour}:00", "AM_${hour}:00") }
                                // If available times for the selected date exist in placeholder map, filter, otherwise show all
                                val availableForDate = selectedDate?.let { d -> placeholderAvailableTimesByDate.value[d] }
                                val amFiltered = availableForDate?.filter { it.startsWith("AM_") }?.map { valStr -> Pair(valStr.removePrefix("AM_"), valStr) } ?: amItems

                                TimeGrid(timeItems = amFiltered, selected = selectedTime, onSelect = { t -> selectedTime = t })

                                Spacer(Modifier.height(8.dp))
                                Text(text = "오후", style = MaterialTheme.typography.titleSmall)
                                val pmItems = (1..12).map { hour -> Pair("${hour}:00", "PM_${hour}:00") }
                                val pmFiltered = availableForDate?.filter { it.startsWith("PM_") }?.map { valStr -> Pair(valStr.removePrefix("PM_"), valStr) } ?: pmItems

                                TimeGrid(timeItems = pmFiltered, selected = selectedTime, onSelect = { t -> selectedTime = t })

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
                            "home" -> navController?.navigate("member_home")
                            "booking" -> navController?.navigate("reservation_model_select")
                            "reservations" -> navController?.navigate("reservations")
                            "mypage" -> navController?.navigate("mypage")
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
private fun TimeGrid(timeItems: List<Pair<String, String>>, selected: String?, onSelect: (String?) -> Unit) {
    Column {
        timeItems.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { (display, value) ->
                    val isSelected = value == selected
                    OutlinedButton(
                        onClick = { if (isSelected) onSelect(null) else onSelect(value) },
                        modifier = Modifier.size(width = 100.dp, height = 35.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                    ) {
                        Text(text = display, style = MaterialTheme.typography.bodySmall, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}





