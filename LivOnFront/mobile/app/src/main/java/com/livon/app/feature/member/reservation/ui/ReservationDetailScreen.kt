package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Gray
import com.livon.app.ui.theme.Gray2
import com.livon.app.ui.theme.Sub2

/**
 * 상세 화면 타입:
 * - Current: 현재 예약
 * - PastPersonal: 지난 예약(개인 상담)
 * - PastGroup: 지난 예약(그룹/클래스)
 */
enum class ReservationDetailType { Current, PastPersonal, PastGroup }

data class CoachMini(
    val name: String,
    val title: String,        // 트레이너/간호사 등
    val specialties: String,  // 체형 교정 / 재활 전문가 등
    val workplace: String     // 근무지
)

data class SessionInfo(
    val dateText: String,     // 예) 10월 16일(수)
    val timeText: String,     // 예) 오후 2:00 - 3:00
    val modelText: String? = null, // 예) 체형교정
    val appliedText: String? = null // 예) 신청 인원: 7/10 (잔여 3)
)

@Composable
fun ReservationDetailScreen(
    type: ReservationDetailType,
    coach: CoachMini,
    session: SessionInfo?,
    aiSummary: String? = null, // Past*에서만 노출
    qnas: List<String>,
    onBack: () -> Unit = {},
    onDelete: () -> Unit = {},     // Past* 우상단 '삭제'
    onSeeCoach: () -> Unit = {},   // '코치 보기'
    onSeeAiDetail: () -> Unit = {},// AI 상세 보기
) {
    Scaffold(
        topBar = {
            DetailTopBar(
                title = "예약 상세",
                showDelete = type != ReservationDetailType.Current,
                onBack = onBack,
                onDelete = onDelete
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 상단 코치 카드
            CoachHeaderCard(coach = coach, onSeeCoach = onSeeCoach)

            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            when (type) {
                ReservationDetailType.Current -> {
                    SectionTitle("상담")
                    SessionBlock(session)
                }
                ReservationDetailType.PastPersonal -> {
                    SectionTitle("분석 결과")
                    AiSummaryBlock(
                        summary = aiSummary ?: "AI 분석 결과 요약이 없습니다.",
                        onSeeDetail = onSeeAiDetail
                    )
                }
                ReservationDetailType.PastGroup -> {
                    SectionTitle("클래스")
                    SessionBlock(session)
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            SectionTitle(if (type == ReservationDetailType.PastPersonal) "Q&A" else "등록한 Q&A")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                qnas.forEach { q ->
                    QnaCard(q)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/* ============ 작은 구성요소들 ============ */

@Composable
private fun DetailTopBar(
    title: String,
    showDelete: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(shadowElevation = 0.dp, tonalElevation = 0.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                Text("〈", fontSize = 18.sp, color = Gray2)
            }
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (showDelete) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    Text("삭제", color = Gray2)
                }
            }
        }
    }
}

@Composable
private fun CoachHeaderCard(
    coach: CoachMini,
    onSeeCoach: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측: 이름/직함/전문분야
            Column(Modifier.weight(1f)) {
                Text(
                    text = coach.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gray2
                    )
                )
                Text(
                    text = "${coach.title}  /  ${coach.specialties}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "근무: ${coach.workplace}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            // 우측: 아바타
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Sub2)
            )
        }

        // 우측 정렬 '코치 보기'
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = onSeeCoach,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(7.dp),
            ) {
                Text(
                    "코치 보기",
                    style = MaterialTheme.typography.labelSmall.copy(color = Gray2)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            color = Gray2,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
    )
}

@Composable
private fun SessionBlock(session: SessionInfo?) {
    if (session == null) return
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = "상담 일정 일시: ",
                style = MaterialTheme.typography.bodyMedium.copy(color = Gray2, fontWeight = FontWeight.Medium)
            )
            Text(
                text = "${session.dateText}   ${session.timeText}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Gray2)
            )
        }
        session.modelText?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall.copy(color = Gray2))
        }
        session.appliedText?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
private fun AiSummaryBlock(summary: String, onSeeDetail: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "AI 분석 결과 요약:",
            style = MaterialTheme.typography.bodyMedium.copy(color = Gray2, fontWeight = FontWeight.Medium)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            summary,
            style = MaterialTheme.typography.bodyMedium.copy(color = Gray2)
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = onSeeDetail,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(7.dp),
            ) {
                Text("상세 보기", style = MaterialTheme.typography.labelSmall.copy(color = Gray2))
            }
        }
    }
}

@Composable
private fun QnaCard(question: String) {
    Surface(
        color = Color(0xFFF4B2AA), // 피그마 핑크톤 근사치
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE19C93)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Q.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Gray2
                )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium.copy(color = Gray2)
            )
        }
    }
}

/* ================== 프리뷰 ================== */

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
            modelText = "체형 교정",
            appliedText = "신청 인원: 7/10 (잔여 3)"
        ),
        qnas = listOf(
            "척추 측만증에 도움되는 운동 알려주세요",
            "허리 통증 완화 운동이 포함되나요?"
        )
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
        aiSummary = "00님의 평균 심박수는 안정적이며, 지난 상담 이후 스트레스 지표가 15% 개선되었습니다.",
        qnas = listOf(
            "상담 중 식단 조언도 받을 수 있나요?",
            "자다가 자주 깨는데 이런 상황을 어떻게 줄일까요",
            "자다가 자주 깨는데 이런 상황을 어떻게 줄일까요"
        )
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
            modelText = "주제: 하체 강화",
            appliedText = "장소: PT룸 A / 인원 8명"
        ),
        qnas = listOf(
            "어깨가 자주 굳어요.",
            "평균보다 팔꿈치가 짧아서 고민"
        )
    )
}
