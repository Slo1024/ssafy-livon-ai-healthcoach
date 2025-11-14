package com.livon.app.feature.member.reservation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    val coachWorkplace: String? = null,
    val timeText: String,         // "오전 9:00 ~ 10:00"
    val classIntro: String,
    val imageResId: Int? = null,
    val classImageUrl: String? = null,

    // coach id (nullable) - used for navigation to coach detail
    val coachId: String? = null,

    // coach image URL from server (nullable). If provided, UI should show remote image; otherwise use local res fallback.
    val coachProfileImageUrl: String? = null,

    // QnA lines submitted when reserving (preQnA split by newline). Used on detail page.
    val qnas: List<String> = emptyList(),

    // AI summary content from server (nullable). Used to show AiResultScreen when present.
    val aiSummary: String? = null,

    // 현재 예약 전용
    val isLive: Boolean = false,  // 진행중/임박
    val startAtIso: String? = null, // ISO time from server
    val sessionId: String? = null, // live session id if coach created

    // 지난 예약 전용
    val sessionTypeLabel: String? = null, // "그룹 상담" | "개인 상담"
    val hasAiReport: Boolean = false,     // 개인상담 + 리포트 있을 때만 AI 버튼

    // convenience flag to indicate personal consultation
    val isPersonal: Boolean = false
)

@Composable
fun ReservationStatusScreen(
    current: List<ReservationUi>,
    past: List<ReservationUi>,
    onBack: () -> Unit,
    // [핵심 수정] onDetail 파라미터에 isPast (Boolean) 추가
    onDetail: (item: ReservationUi, isPast: Boolean) -> Unit,
    onCancel: (ReservationUi) -> Unit,
    onJoin: (ReservationUi) -> Unit,
    onAiAnalyze: (ReservationUi) -> Unit
) {
    // Ensure system back (device back) behaves the same as TopBar back
    BackHandler {
        onBack()
    }

    var tab by remember { mutableStateOf(ReservationTab.CURRENT) }
    // Cancellation modal state: which reservation is targeted for cancellation
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelTarget by remember { mutableStateOf<ReservationUi?>(null) }

    // Debug toggle: when true, force showJoin for all cards so developer can test "입장하기" 버튼
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
                PrimaryTabRow(selectedTabIndex = tab.ordinal) {
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
                                if (item.isLive) "진행중" else "${daysLeft}일 후 상담"

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
                                imageUrl = item.classImageUrl,
                                coachProfileResId = null,
                                coachProfileImageUrl = item.coachProfileImageUrl,
                                showCoachProfile = item.isPersonal || (item.sessionTypeLabel == null && item.coachProfileImageUrl != null),
                                // [핵심 수정] onDetail 호출 시 isPast=false 전달
                                onDetail = { onDetail(item, false) },
                                onCancel = if (!item.isLive) ({ cancelTarget = item; showCancelDialog = true }) else null,
                                // If debugForceJoin is enabled, provide onJoin even when not live
                                onJoin   = if (item.isLive || debugForceJoin.value)  ({ onJoin(item) })   else null,
                                onAiAnalyze = null,
                                showJoin = item.isLive || debugForceJoin.value,
                                showCancel = !item.isLive && !debugForceJoin.value,
                                showAiButton = false,
                                dividerBold = true
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
                                imageUrl = item.classImageUrl,
                                coachProfileResId = null,
                                coachProfileImageUrl = item.coachProfileImageUrl,
                                showCoachProfile = item.isPersonal,
                                // [핵심 수정] onDetail 호출 시 isPast=true 전달
                                onDetail = { onDetail(item, true) },
                                onCancel = null,
                                onJoin = null,
                                onAiAnalyze = if (showAI) ({ onAiAnalyze(item) }) else null,
                                showJoin = false,
                                showCancel = false,
                                showAiButton = showAI,
                                dividerBold = false
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

    // Render cancellation modal outside of CommonScreenC so it overlays the entire screen (including TopBar)
    if (showCancelDialog && cancelTarget != null) {
        ReservationCompleteDialog(
            onDismiss = { showCancelDialog = false; cancelTarget = null },
            onConfirm = {
                try {
                    cancelTarget?.let { onCancel(it) }
                } catch (_: Throwable) {}
                showCancelDialog = false
                cancelTarget = null
            },
            titleText = "예약을 취소하시겠습니까?",
            subtitleText = null,
            showCancelButton = true,
            confirmLabel = "확인",
            cancelLabel = "취소"
        )
    }
}

/* ---------- 유틸 ---------- */
private fun formatKoreanDate(date: LocalDate): String {
    // 예: 9.13 (금)
    val dayKor = "월화수목금토일"[date.dayOfWeek.value % 7]
    return "${date.month.value}.${date.dayOfMonth} (${dayKor})"
}

/* ---------- Preview (여러 경우) ---------- */

// Preview 코드는 파라미터가 변경되었으므로 onDetail 부분도 수정합니다.
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
            onDetail = { _, _ -> /* Preview에서는 동작 없음 */ },
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
            onDetail = { _, _ -> /* Preview에서는 동작 없음 */ },
            onCancel = {},
            onJoin = {},
            onAiAnalyze = {}
        )
    }
}
