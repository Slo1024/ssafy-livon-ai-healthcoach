package com.livon.app.feature.member.reservation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.ui.Alignment
import com.livon.app.BuildConfig

enum class ReservationTab { CURRENT, PAST }

/** UI 표시용 모델 */
data class ReservationUi(
    val id: String,
    val date: LocalDate,
    val className: String,
    val coachName: String,
    val coachRole: String,
    val coachIntro: String,
    val timeText: String,         // "오전 9:00 ~ 10:00"
    val classIntro: String,
    val imageResId: Int? = null,

    // 현재 예약 전용
    val isLive: Boolean = false,  // 진행중/임박
    val startAtIso: String? = null, // ISO time from server
    val sessionId: String? = null, // live session id if coach created

    // 지난 예약 전용
    val sessionTypeLabel: String? = null, // "그룹 상담" | "개인 상담"
    val hasAiReport: Boolean = false      // 개인상담 + 리포트 있을 때만 AI 버튼
)

@Composable
fun ReservationStatusScreen(
    current: List<ReservationUi>,
    past: List<ReservationUi>,
    onBack: () -> Unit,
    onDetail: (ReservationUi) -> Unit,
    onCancel: (ReservationUi) -> Unit,
    onJoin: (ReservationUi) -> Unit,
    onAiAnalyze: (ReservationUi) -> Unit
) {
    // Ensure system back (device back) behaves the same as TopBar back
    BackHandler {
        onBack()
    }

    var tab by remember { mutableStateOf(ReservationTab.CURRENT) }

    // Debug toggle: when true, force showJoin for all cards so developer can test "입장하기" button
    val debugForceJoin = remember { mutableStateOf(false) }

    CommonScreenC(
        topBar = { TopBar(title = "예약 현황", onBack = onBack) }
    ) {
        // Wrap in Box to allow overlaying a debug FAB
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                TabRow(selectedTabIndex = tab.ordinal) {
                    Tab(
                        selected = tab == ReservationTab.CURRENT,
                        onClick = { tab = ReservationTab.CURRENT },
                        text = { Text("현재 예약") }
                    )
                    Tab(
                        selected = tab == ReservationTab.PAST,
                        onClick = { tab = ReservationTab.PAST },
                        text = { Text("지난 예약") }
                    )
                }

                Spacer(Modifier.height(8.dp))

                val list = if (tab == ReservationTab.CURRENT) current else past

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { item ->
                        if (tab == ReservationTab.CURRENT) {
                            // n일 후 상담 텍스트
                            val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), item.date).toInt()
                            val rightLabel =
                                if (item.isLive) "진행중" else "${'$'}{daysLeft}일 후 상담"

                            ReservationCard(
                                headerLeft = formatKoreanDate(item.date),
                                headerRight = rightLabel,
                                headerRightIsLive = item.isLive,
                                className = item.className,
                                coachName = item.coachName,
                                coachRole = item.coachRole,
                                coachIntro = item.coachIntro,
                                timeText = item.timeText,
                                classIntro = item.classIntro,
                                imageResId = item.imageResId,
                                onDetail = { onDetail(item) },
                                onCancel = if (!item.isLive) ({ onCancel(item) }) else null,
                                // If debugForceJoin is enabled, provide onJoin even when not live
                                onJoin   = if (item.isLive || debugForceJoin.value)  ({ onJoin(item) })   else null,
                                onAiAnalyze = null,
                                showJoin = item.isLive || debugForceJoin.value,          // 진행중일 때 이미지 아래 세션버튼
                                showCancel = !item.isLive && !debugForceJoin.value,       // 임박/진행중이면 취소 X; when debug forcing join, hide cancel
                                showAiButton = false,
                                dividerBold = true               // 현재 예약: 굵기 1(요구상 동일)
                            )
                        } else {
                            // 지난 예약: AI 버튼은 '개인 상담' 이면서 hasAiReport=true 일 때만
                            val isPersonal = item.sessionTypeLabel == "개인 상담"
                            val showAI = isPersonal && item.hasAiReport

                            ReservationCard(
                                headerLeft = formatKoreanDate(item.date),
                                headerRight = item.sessionTypeLabel ?: "",
                                headerRightIsLive = false,
                                className = item.className,
                                coachName = item.coachName,
                                coachRole = item.coachRole,
                                coachIntro = item.coachIntro,
                                timeText = item.timeText,
                                classIntro = item.classIntro,
                                imageResId = item.imageResId,
                                onDetail = { onDetail(item) },
                                onCancel = null,
                                onJoin = null,
                                onAiAnalyze = if (showAI) ({ onAiAnalyze(item) }) else null,
                                showJoin = false,
                                showCancel = false,
                                showAiButton = showAI,
                                dividerBold = false             // 지난 예약: 보더 1
                            )
                        }
                    }
                }
            }

            // Debug FAB overlay - visible only in debug builds
            if (BuildConfig.DEBUG) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                    FloatingActionButton(
                        onClick = { debugForceJoin.value = !debugForceJoin.value }
                    ) {
                        Text(if (debugForceJoin.value) "디버그: Join ON" else "디버그: Join OFF")
                    }
                }
            }
        }
    }
}

/* ---------- 유틸 ---------- */
private fun formatKoreanDate(date: LocalDate): String {
    // 예: 9.13 (금)
    val dayKor = "월화수목금토일"[date.dayOfWeek.value % 7]
    return "${date.month.value}.${date.dayOfMonth} (${dayKor})"
}

/* ---------- Preview (여러 경우) ---------- */

@Preview(showBackground = true, name = "현재 예약 – 일반")
@Composable
private fun Preview_Current_Normal() {
    val today = LocalDate.now()
    LivonTheme {
        ReservationStatusScreen(
            current = listOf(
                ReservationUi(
                    id = "c1",
                    date = today.plusDays(3),
                    className = "체형 교정",
                    coachName = "이싸피",
                    coachRole = "PT",
                    coachIntro = "자세 교정 전문",
                    timeText = "오전 9:00 ~ 10:00",
                    classIntro = "자세 교정 기반 클래스",
                    startAtIso = null,
                    sessionId = null
                )
            ),
            past = emptyList(),
            onBack = {},
            onDetail = {},
            onCancel = {},
            onJoin = {},
            onAiAnalyze = {}
        )
    }
}

@Preview(showBackground = true, name = "현재 예약 – 진행중(세션 버튼)")
@Composable
private fun Preview_Current_Live() {
    val today = LocalDate.now()
    LivonTheme {
        ReservationStatusScreen(
            current = listOf(
                ReservationUi(
                    id = "c2",
                    date = today,
                    className = "필라테스",
                    coachName = "김싸피",
                    coachRole = "필라테스",
                    coachIntro = "유연성/자세 전문가",
                    timeText = "오후 3:00 ~ 4:00",
                    classIntro = "자세 교정/체형 교정",
                    isLive = true,
                    startAtIso = null,
                    sessionId = null
                )
            ),
            past = emptyList(),
            onBack = {},
            onDetail = {},
            onCancel = {},
            onJoin = {},
            onAiAnalyze = {}
        )
    }
}
