package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Gray2
import com.livon.app.ui.theme.Spacing
import com.livon.app.ui.theme.Sub2

enum class ReservationDetailType { Current, PastPersonal, PastGroup }

data class CoachMini(
    val name: String,
    val title: String,          // 트레이너/간호사 등
    val specialties: String,    // 체형 교정 / 재활 전문가 등
    val workplace: String,      // 근무지(소속)
    val profileResId: Int? = null // null -> ic_noprofile
)

data class SessionInfo(
    val dateText: String,        // 예) 10월 16일(수)
    val timeText: String,        // 예) 오후 2:00 - 3:00
    val modelText: String? = null,    // 상담명/클래스 소개
    val appliedText: String? = null   // 신청/참여 인원 등
)

@Composable
fun ReservationDetailScreen(
    type: ReservationDetailType,
    coach: CoachMini,
    session: SessionInfo?,
    aiSummary: String? = null,          // PastPersonal에서 사용
    qnas: List<String>,
    onBack: () -> Unit = {},
    onDelete: () -> Unit = {},          // Past* 우상단 '삭제'
    onSeeCoach: () -> Unit = {},
    onSeeAiDetail: () -> Unit = {},
    onActivateStreaming: () -> Unit = {},
    onEnterSession: () -> Unit = {},
    enterEnabled: Boolean = true
) {
    CommonScreenC(
        // ── TopBar: 지난 예약일 때만 오른쪽 "삭제" 노출
        topBar = { modifier ->
            Box(modifier) {
                TopBar(title = "예약 상세", onBack = onBack)
                if (type != ReservationDetailType.Current) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = Spacing.Horizontal)
                    ) {
                        Text("삭제", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                    }
                }
            }
        },
        // 상단바 바로 아래 3개 박스는 full-bleed로(양옆 마진 침범)
        fullBleedContent = {
            val outerScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(outerScroll),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1) 코치 박스 (항상 표시) - 325x140 규격에 맞춰 높이 고정
                CoachBox(
                    coach = coach,
                    onSeeCoach = onSeeCoach,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                // 2) 타입별 두번째 박스 - 325x178 높이 고정
                when (type) {
                    ReservationDetailType.Current -> {
                        SessionInfoBox(
                            title = "상담",
                            session = session,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(178.dp)
                        )
                    }
                    ReservationDetailType.PastPersonal -> {
                        AiResultBox(
                            title = "분석 결과",
                            summaryLabel = "AI 분석 결과 요약:",
                            summary = aiSummary ?: "AI 분석 결과 요약이 없습니다.",
                            onSeeDetail = onSeeAiDetail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(178.dp)
                        )
                    }
                    ReservationDetailType.PastGroup -> {
                        ClassInfoBox(
                            title = "클래스",
                            session = session,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(178.dp)
                        )
                    }
                }

                // 3) 등록한 Q&A 박스 - 325x1310, 내부 스크롤
                QnaBox(
                    title = if (type == ReservationDetailType.PastPersonal) "Q&A" else "등록한 Q&A",
                    qnas = qnas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1310.dp)
                )

                // 현재 예약일 때만 하단 고정 버튼
                if (type == ReservationDetailType.Current) {
                    Spacer(Modifier.height(12.dp))
                    PrimaryButtonBottom(
                        text = "세션 입장하기",
                        enabled = enterEnabled,
                        onClick = {
                            onActivateStreaming() // 스트리밍 초기화/활성화
                            onEnterSession()      // 실제 입장 네비게이션
                        }
                    )
                }
            }
        }
    ) {
        // CommonScreenC의 content 블록은 사용하지 않음(가로 패딩이 붙기 때문)
    }
}

/* ─────────── 공통 카드 컨테이너 (외각선 0.6dp, 내부 패딩 16/12) ─────────── */

@Composable
private fun CardContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background, // basic(흰색)
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            content = content
        )
    }
}

/* ─────────── 1) 코치 박스 ─────────── */

@Composable
private fun CoachBox(
    coach: CoachMini,
    onSeeCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(modifier) {
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = coach.name,
                        color = MaterialTheme.colorScheme.primary, // main
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = coach.title,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = coach.specialties,
                    color = Gray2,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "근무: ${coach.workplace}",
                        color = Color.Black,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onSeeCoach,
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .width(77.dp)
                            .height(21.dp)
                    ) {
                        Text(
                            text = "코치 보기",
                            color = Gray2,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            val resId = coach.profileResId ?: R.drawable.ic_noprofile
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Sub2),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = resId),
                    contentDescription = "coach_profile",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/* ─────────── 2A) 현재예약: 상담 상세 ─────────── */

@Composable
private fun SessionInfoBox(
    title: String,
    session: SessionInfo?,
    modifier: Modifier = Modifier
) {
    CardContainer(modifier) {
        Text(text = title, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (session != null) {
            Text(
                text = "상담 예정 일시 : ${session.dateText}, ${session.timeText}",
                color = Gray2,
                fontSize = 12.sp
            )
            session.modelText?.let {
                Spacer(Modifier.height(6.dp))
                Text(text = it, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            session.appliedText?.let {
                Spacer(Modifier.height(4.dp))
                Text(text = it, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Text(text = "일정 정보가 없습니다.", color = Gray2, fontSize = 12.sp)
        }
    }
}

/* ─────────── 2B) 지난예약(개인): 분석 결과 ─────────── */

@Composable
private fun AiResultBox(
    title: String,
    summaryLabel: String,
    summary: String,
    onSeeDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(modifier) {
        Text(text = title, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(text = summaryLabel, color = Color.Black, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Text(text = summary, color = Color.Black, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = onSeeDetail,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .width(77.dp)
                    .height(21.dp)
            ) {
                Text(text = "상세 보기", color = Gray2, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/* ─────────── 2C) 지난예약(그룹): 클래스 정보 ─────────── */

@Composable
private fun ClassInfoBox(
    title: String,
    session: SessionInfo?,
    modifier: Modifier = Modifier
) {
    CardContainer(modifier) {
        Text(text = title, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (session != null) {
            Text(
                text = "일시 : ${session.dateText}, ${session.timeText}",
                color = Gray2,
                fontSize = 12.sp
            )
            session.modelText?.let {
                Spacer(Modifier.height(6.dp))
                Text(text = it, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            session.appliedText?.let {
                Spacer(Modifier.height(4.dp))
                Text(text = it, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Text(text = "클래스 정보가 없습니다.", color = Gray2, fontSize = 12.sp)
        }
    }
}

/* ─────────── 3) 등록한 Q&A: 내부 스크롤 ─────────── */

@Composable
private fun QnaBox(
    title: String,
    qnas: List<String>,
    modifier: Modifier = Modifier
) {
    CardContainer(modifier) {
        Text(text = title, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        val innerScroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(innerScroll),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            qnas.forEach { q ->
                Surface(
                    color = Color(0xFFF4B2AA), // 피그마 핑크톤 근사치
                    shape = RoundedCornerShape(10.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Color(0xFFE19C93)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(text = q, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/* ─────────── 프리뷰 ─────────── */

@Preview(showBackground = true, name = "현재 예약 – 상담 탭")
@Composable
private fun Preview_Current() = PreviewSurface {
    ReservationDetailScreen(
        type = ReservationDetailType.Current,
        coach = CoachMini(
            name = "이싸피",
            title = "트레이너",
            specialties = "체형 교정 / 재활 전문가",
            workplace = "스윙바디 피트니스 영지점"
        ),
        session = SessionInfo(
            dateText = "10월 16일(수)",
            timeText = "오후 2:00 - 3:00",
            modelText = "상담 명: 체형 교정",
            appliedText = "신청 인원: 7/10 (잔여 3)"
        ),
        qnas = listOf("척추 측만증에 도움되는 운동 알려주세요", "허리 통증 완화 운동이 포함되나요?"),
        enterEnabled = true
    )
}

@Preview(showBackground = true, name = "지난 예약 – 개인 상담(분석 결과)")
@Composable
private fun Preview_PastPersonal() = PreviewSurface {
    ReservationDetailScreen(
        type = ReservationDetailType.PastPersonal,
        coach = CoachMini(
            name = "박소연",
            title = "간호사",
            specialties = "영양 관리 / 수면 코칭",
            workplace = "스윙바디 웰니스케어 센터"
        ),
        session = null,
        aiSummary = "평균 심박수 안정, 스트레스 지표 15% 개선.",
        qnas = listOf("상담 중 식단 조언도 받을 수 있나요?", "자다가 자주 깨는데 어떻게 줄일까요")
    )
}

@Preview(showBackground = true, name = "지난 예약 – 그룹(클래스)")
@Composable
private fun Preview_PastGroup() = PreviewSurface {
    ReservationDetailScreen(
        type = ReservationDetailType.PastGroup,
        coach = CoachMini(
            name = "최민호",
            title = "트레이너",
            specialties = "코어 / 교정",
            workplace = "스윙바디 피트니스"
        ),
        session = SessionInfo(
            dateText = "3월 15일(금)",
            timeText = "오후 2:00 - 3:00",
            modelText = "클래스 소개: 하체 강화 루틴",
            appliedText = "참여 인원: 8/10 명"
        ),
        qnas = listOf("어깨가 자주 굳어요.", "평균보다 팔꿈치가 짧아서 고민")
    )
}
