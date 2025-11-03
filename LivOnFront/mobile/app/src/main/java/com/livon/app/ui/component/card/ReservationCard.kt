package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.theme.Gray2   // 프로젝트 테마에 맞춰주세요
import com.livon.app.ui.theme.Main   // main 색상
import androidx.compose.ui.composed

@Composable
private fun PillOutlineButton(
    text: String,
    width: Dp,
    height: Dp,
    radius: Dp,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(radius))
            .border(1.dp, borderColor, RoundedCornerShape(radius))
            .background(Color.White)
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun SessionJoinButton(
    text: String = "세션 입장하기",
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(27.dp)
            .clip(RoundedCornerShape(5.dp))
            .border(1.dp, Main, RoundedCornerShape(5.dp))
            .background(Color.White)
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_session),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Main
            )
        }
    }
}

/** 공용 카드 (화면 배경과 동일, radius=0) */
@Composable
fun ReservationCard(
    // 헤더
    headerLeft: String,             // 예) "10.20 (금)" / "12.01 (일)"
    headerRight: String,            // 예) "n일 후 상담" / "진행중" / "그룹 상담" / "개인 상담"
    headerRightIsLive: Boolean,     // "진행중" 표시 시 빨간 점

    // 본문
    className: String,
    coachName: String,
    coachRole: String,
    coachIntro: String,
    timeText: String,
    classIntro: String,
    imageResId: Int? = null,

    // 버튼/동작
    onDetail: () -> Unit,
    onCancel: (() -> Unit)? = null,        // 현재예약에서만
    onJoin:   (() -> Unit)? = null,        // 임박/진행중에서만 (이미지 하단)
    onAiAnalyze: (() -> Unit)? = null,     // 지난예약 + 개인상담 + 리포트 있을 때만

    // 노출 제어
    showJoin: Boolean = false,
    showCancel: Boolean = false,
    showAiButton: Boolean = false,

    // 구분선 굵기: 현재예약=굵게(1), 지난예약=보더(1)
    dividerBold: Boolean
) {
    val img = imageResId ?: R.drawable.ic_classphoto

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(0.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            /* HEADER */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerLeft,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (headerRightIsLive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Red)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = headerRight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Divider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp // 요구: border:outline, 굵기 1
            )

            Spacer(Modifier.height(10.dp))

            /* CONTENT */
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = className, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text(text = coachName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Main)
                    Spacer(Modifier.height(2.dp))
                    Text(text = "$coachRole · $coachIntro", fontSize = 10.sp, color = Gray2)
                    Spacer(Modifier.height(6.dp))
                    Text(text = timeText, fontSize = 10.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(text = classIntro, fontSize = 11.sp, color = Gray2)
                }

                Spacer(Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = img),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    if (showJoin && onJoin != null) {
                        Spacer(Modifier.height(8.dp))
                        SessionJoinButton(onClick = onJoin)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            /* BUTTONS (가로 배치) */
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 예약 상세 (항상 노출)
                PillOutlineButton(
                    text = "예약 상세",
                    width = 80.dp,
                    height = 25.dp,
                    radius = 20.dp,
                    borderColor = Main,
                    textColor = Main,
                    onClick = onDetail
                )

                // 예약 취소 (현재 예약 + 임박/진행중이 아닌 경우)
                if (showCancel && onCancel != null) {
                    PillOutlineButton(
                        text = "예약 취소",
                        width = 80.dp,
                        height = 25.dp,
                        radius = 20.dp,
                        borderColor = MaterialTheme.colorScheme.outline, // border
                        textColor = Gray2,
                        onClick = onCancel
                    )
                }

                // AI 분석 (지난 예약 + 개인상담 + 리포트 有)
                if (showAiButton && onAiAnalyze != null) {
                    PillOutlineButton(
                        text = "AI 분석",
                        width = 80.dp,
                        height = 25.dp,
                        radius = 20.dp,
                        borderColor = Main,
                        textColor = Main,
                        onClick = onAiAnalyze
                    )
                }
            }
        }
    }
}

/* ─────────────────────────────────────────────
   Ripple 없는 Click (⚠️ @Composable 필요)
───────────────────────────────────────────── */
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = onClick
    )
}
