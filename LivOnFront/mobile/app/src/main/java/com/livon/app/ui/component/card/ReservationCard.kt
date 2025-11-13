package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.livon.app.R
import com.livon.app.ui.theme.Gray2   // 프로젝트 테마에 맞춰주세요
import com.livon.app.ui.theme.Main   // main 색상

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
    imageUrl: String? = null,

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
            // 날짜를 구분선 위로 이동
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = headerLeft, fontSize = 11.sp, color = Gray2)
            }
            Spacer(Modifier.height(6.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = if (dividerBold) 1.dp else 1.dp
            )

            Spacer(Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // date removed from here (now above divider)
                        Spacer(Modifier.height(4.dp))
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

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
                        // status aligned to top-end above the image
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically) {
                            if (headerRightIsLive) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.Red)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(text = headerRight, fontSize = 10.sp, color = Color.Black)
                        }

                        Spacer(Modifier.height(8.dp))

                        if (!imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_classphoto),
                                error = painterResource(id = R.drawable.ic_classphoto)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = img),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            /* BUTTONS (가로 배치) */
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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

                // session join on same row as reservation 상세
                if (showJoin && onJoin != null) {
                    SessionJoinButton(onClick = onJoin)
                }
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

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}



/* ─────────────────────────────────────────────
    #1 현재 예약 – 일반 (n일 후 상담 + 취소 가능)
───────────────────────────────────────────── */
@Preview(showBackground = true, name = "현재 예약 – 일반")
@Composable
private fun Preview_ReservationCard_Current_Normal() {
    ReservationCard(
        headerLeft = "10.20 (금)",
        headerRight = "3일 후 상담",
        headerRightIsLive = false,
        className = "체형 교정",
        coachName = "이싸피",
        coachRole = "PT",
        coachIntro = "자세 교정 전문",
        timeText = "오전 9:00 ~ 10:00",
        classIntro = "자세 교정 중심 클래스",
        onDetail = {},
        onCancel = {},
        showCancel = true,
        dividerBold = true
    )
}

/* ─────────────────────────────────────────────
    #2 현재 예약 – 진행중 (임박, Join 버튼)
───────────────────────────────────────────── */
@Preview(showBackground = true, name = "현재 예약 – 진행중")
@Composable
private fun Preview_ReservationCard_Current_Live() {
    ReservationCard(
        headerLeft = "10.20 (금)",
        headerRight = "진행중",
        headerRightIsLive = true,
        className = "저녁 스트레칭",
        coachName = "김싸피",
        coachRole = "트레이너",
        coachIntro = "근골격 교정",
        timeText = "오후 4:00 ~ 5:00",
        classIntro = "통증 완화 · 심폐기능 향상",
        onDetail = {},
        onJoin = {},
        showJoin = true,
        dividerBold = true
    )
}

/* ─────────────────────────────────────────────
    #3 지난 예약 – 개인 상담 + AI분석
───────────────────────────────────────────── */
@Preview(showBackground = true, name = "지난 예약 – 개인상담 + AI")
@Composable
private fun Preview_ReservationCard_Past_Personal_AI() {
    ReservationCard(
        headerLeft = "10.10 (토)",
        headerRight = "개인 상담",
        headerRightIsLive = false,
        className = "코어 트레이닝",
        coachName = "최코치",
        coachRole = "코치",
        coachIntro = "코어 · 자세 전문가",
        timeText = "오전 10:00 ~ 11:00",
        classIntro = "개인 집중 코어 트레이닝",
        onDetail = {},
        onAiAnalyze = {},
        showAiButton = true,
        dividerBold = false
    )
}

/* ─────────────────────────────────────────────
    #4 지난 예약 – 그룹 상담 (AI 없음)
───────────────────────────────────────────── */
@Preview(showBackground = true, name = "지난 예약 – 그룹 상담")
@Composable
private fun Preview_ReservationCard_Past_Group() {
    ReservationCard(
        headerLeft = "09.02 (월)",
        headerRight = "그룹 상담",
        headerRightIsLive = false,
        className = "모닝 필라테스",
        coachName = "박코치",
        coachRole = "필라테스",
        coachIntro = "스트레칭 중심",
        timeText = "오전 8:00 ~ 9:00",
        classIntro = "부드러운 모닝 스트레칭",
        onDetail = {},
        dividerBold = false
    )
}
