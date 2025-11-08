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
                    Spacer(Modifier.height(8.dp))

                    val avatarPainter = rememberAsyncImagePainter(coach.avatarUrl ?: "")
                    Image(
                        painter = avatarPainter,
                        contentDescription = "coach",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(text = coach.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(8.dp))

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
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

                    Spacer(Modifier.height(20.dp))

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

                            CalendarMonth(
                                yearMonth = currentMonth,
                                selected = selectedDate,
                                onSelect = { date ->
                                    // selecting a date clears any previously chosen time and hides time selection
                                    selectedDate = if (selectedDate == date) null else date
                                    selectedTime = null
                                    showTimeSelection = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(12.dp))

                            // Note: the bottom persistent button handles moving from date->time phase and final reservation.

                            if (showTimeSelection) {
                                Spacer(Modifier.height(8.dp))
                                Text(text = "오전", style = MaterialTheme.typography.titleSmall)
                                TimeGrid(times = (1..12).map { "%d:00".format(it) }, selected = selectedTime, onSelect = { t -> selectedTime = t })

                                Spacer(Modifier.height(8.dp))
                                Text(text = "오후", style = MaterialTheme.typography.titleSmall)
                                TimeGrid(times = (1..12).map { "%d:00".format(it) }, selected = selectedTime, onSelect = { t -> selectedTime = t })

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
        // Group consultation: simple scaffold with HomeNavBar at bottom and no calendar/time
        Scaffold(
            topBar = { TopBar(title = "상세 정보", onBack = onBack) },
            bottomBar = {
                HomeNavBar(currentRoute = null, navController = navController, onNavigate = { route ->
                    // default nav mapping
                    when (route) {
                        "home" -> navController?.navigate("member_home")
                        "booking" -> navController?.navigate("reservation_model_select")
                        "reservations" -> navController?.navigate("reservations")
                        "mypage" -> navController?.navigate("mypage")
                        else -> {}
                    }
                })
            }
        ) { inner ->
            Column(Modifier.fillMaxSize().padding(inner).padding(20.dp)) {
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
        }
    }

    LaunchedEffect(coachId) { vm.load(coachId) }
}

@Composable
private fun TimeGrid(times: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    Column {
        times.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { time ->
                    val isSelected = time == selected
                    OutlinedButton(
                        onClick = { if (isSelected) onSelect(null) else onSelect(time) },
                        modifier = Modifier.size(width = 70.dp, height = 35.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                    ) {
                        Text(text = time, style = MaterialTheme.typography.bodySmall, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachDetail() = PreviewSurface {
    LivonTheme {
        CoachDetailScreen(coachId = "1", onBack = {}, onReserve = { _, _, _ -> })
    }
}
