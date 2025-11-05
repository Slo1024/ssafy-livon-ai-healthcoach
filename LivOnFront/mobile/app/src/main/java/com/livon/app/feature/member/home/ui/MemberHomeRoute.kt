package com.livon.app.feature.member.home.ui

import com.livon.app.feature.shared.auth.ui.SignupState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.component.navbar.HomeNavBar
import com.livon.app.ui.component.navbar.BottomNavRoute
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.graphicsLayer
import com.livon.app.feature.member.reservation.ui.ReservationUi
import com.livon.app.feature.member.reservation.ui.ReservationCard

/* ---------- Models ---------- */
data class DataMetric(
    val name: String,
    val value: String,
    val average: String
)

data class UpcomingItem(
    val title: String,
    val datetime: String
)

/* ---------- Screen ---------- */
@Composable
fun MemberHomeRoute(
    onTapBooking: () -> Unit,
    onTapReservations: () -> Unit,
    onTapMyPage: () -> Unit,
    metrics: List<DataMetric> = emptyList(),
    upcoming: List<UpcomingItem> = emptyList(),
    upcomingReservations: List<ReservationUi> = emptyList(),
    companyName: String? = "ACME Corp.",
    modifier: Modifier = Modifier
) {
    val mainBlue = Color(0xFF0F74FF)
    val warnRed = Color(0xFFFF5C5C)
    val dividerGray = Color(0xFFDDDDDD)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            HomeNavBar(
                modifier = Modifier.navigationBarsPadding(),
                currentRoute = BottomNavRoute.HOME.routeName,
                onNavigate = { route ->
                    when (route) {
                        BottomNavRoute.BOOKING.routeName -> onTapBooking()
                        BottomNavRoute.RESERVATIONS.routeName -> onTapReservations()
                        BottomNavRoute.MY_PAGE.routeName -> onTapMyPage()
                        BottomNavRoute.HOME.routeName -> { /* already on home */ }
                        else -> { /* noop */ }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            /* --- 상단 LIVON --- */
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = "LIVON",
                    color = mainBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Divider(Modifier.fillMaxWidth(), color = dividerGray, thickness = 1.dp)

            /* --- 프로필/회사 --- */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(shape = RoundedCornerShape(999.dp), modifier = Modifier.size(54.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFEFEFEF))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_noprofile),
                            contentDescription = "profile",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Text(
                    text = if (SignupState.nickname.isBlank()) "회원님" else if (SignupState.nickname.endsWith("님")) SignupState.nickname else "${SignupState.nickname}님",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                companyName?.let {
                    Text(
                        text = it,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Divider(Modifier.fillMaxWidth(), color = dividerGray, thickness = 1.dp)

            /* --- 내 데이터 --- */
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 14.dp, end = 20.dp)
            ) {
                Text(
                    "내 데이터",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    metrics.forEach { metric ->
                        Card(
                            modifier = Modifier
                                .size(width = 140.dp, height = 104.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = metric.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF555555),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = metric.value,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = metric.average,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = dividerGray,
                thickness = 1.dp
            )

            /* --- 상담 / 코칭 예약 --- */
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    "상담 / 코칭 예약",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Reservation action buttons: always visible
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ReservationBigButton(
                        title = "예약 하기",
                        desc = "원하는 시간의 클래스와\n코치를 선택해보세요",
                        bg = mainBlue,
                        onClick = onTapBooking,
                        modifier = Modifier.weight(1f)
                    )
                    ReservationBigButton(
                        title = "예약 현황",
                        desc = "예약한 상담 / 코칭과\n나의 상담 AI 요약을\n확인해 보세요",
                        bg = warnRed,
                        onClick = onTapReservations,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Divider(Modifier.fillMaxWidth(), color = dividerGray, thickness = 1.dp)

            /* --- 다가오는 상담 / 코칭 --- */
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    "다가오는 상담 / 코칭",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // If there are upcoming reservations, show them here as text rows (also allows vertical scrolling of whole screen)
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (upcomingReservations.isEmpty()) {
                        Text(text = "등록된 예약이 없습니다.", color = Color.Gray)
                    } else {
                        upcomingReservations.forEach { item ->
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)) {
                                Text(
                                    text = item.className,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )

                                Spacer(Modifier.height(4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatDateYmd(item.date),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = extractTimeShort(item.timeText),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Components ---------- */

@Composable
private fun ReservationBigButton(
    title: String,
    desc: String,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(128.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(14.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(50.dp)) // 간격을 줄여서 chevron이 더 가까이 오도록 함
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "chevron",
                tint = Color.White,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { scaleX = -1f } // 좌우 반전
            )
        }

        Text(
            text = desc,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        Spacer(
            Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .noRippleClickable(onClick)
        )
    }
}


@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.then(
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    )
}

// helper: format date as yyyy/MM/dd
private fun formatDateYmd(date: java.time.LocalDate): String {
    return "%04d/%02d/%02d".format(date.year, date.monthValue, date.dayOfMonth)
}

// helper: try to extract HH:mm pattern from a timeText like "오후 4:00 ~ 5:00" or "16:40"
private fun extractTimeShort(timeText: String): String {
    // try to find pattern like 16:40 or 04:30
    val regex = Regex("(\\d{1,2}:\\d{2})")
    val match = regex.find(timeText)
    return match?.value ?: timeText
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "MemberHomeRoute")
@Composable
private fun MemberHomeRoutePreview() {
    val dummyMetrics = listOf(
        DataMetric("신장/몸무게", "170cm / 50Kg", "평균: 169cm / 평균: 60Kg"),
        DataMetric("수면시간", "7시간", "평균: 7시간"),
        DataMetric("체지방률", "18%", "평균: 20%"),
        DataMetric("혈압", "120/80", "평균: 122/82")
    )

    val today = java.time.LocalDate.of(2025, 10, 15)
    val sampleReservations = listOf(
        com.livon.app.feature.member.reservation.ui.ReservationUi(
            id = "p1",
            date = today,
            className = "필라테스 클래스",
            coachName = "김코치",
            coachRole = "코치",
            coachIntro = "스트레칭 중심",
            timeText = "16:40",
            classIntro = "저녁 힐링",
            imageResId = null,
            isLive = false
        ),
        com.livon.app.feature.member.reservation.ui.ReservationUi(
            id = "p2",
            date = today.plusDays(2),
            className = "식단 코칭",
            coachName = "박코치",
            coachRole = "영양",
            coachIntro = "식단 설계",
            timeText = "19:30",
            classIntro = "맞춤 식단",
            imageResId = null,
            isLive = false
        )
    )

    LivonTheme {
        MemberHomeRoute(
            onTapBooking = {},
            onTapReservations = {},
            onTapMyPage = {},
            metrics = dummyMetrics,
            upcomingReservations = sampleReservations,
            companyName = "회사이름"
        )
    }
}
