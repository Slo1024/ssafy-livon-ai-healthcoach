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
    nickname: String? = null,
    profileImageUri: android.net.Uri? = null,
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
                // Show server-provided profile image if available, otherwise fallback to ic_noprofile
                Card(shape = RoundedCornerShape(999.dp), modifier = Modifier.size(54.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFEFEFEF))
                    ) {
                        val painter = profileImageUri?.let { coil.compose.rememberAsyncImagePainter(it) }
                            ?: painterResource(id = R.drawable.ic_noprofile)
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "profile",
                            modifier = Modifier.size(54.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Text(
                    text = run {
                        val fromSignup = if (SignupState.nickname.isBlank()) null else if (SignupState.nickname.endsWith("님")) SignupState.nickname else "${SignupState.nickname}님"
                        when {
                            !nickname.isNullOrBlank() -> nickname
                            !fromSignup.isNullOrBlank() -> fromSignup
                            else -> "회원님"
                        }
                    },
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

                // Show metrics as horizontally scrollable cards (one card per metric)
                val metricsToShow = if (metrics.isNotEmpty()) metrics else listOf(
                    DataMetric("키", "-", ""),
                    DataMetric("몸무게", "-", ""),
                    DataMetric("질환", "-", ""),
                    DataMetric("수면 상태", "-", ""),
                    DataMetric("복약 여부", "-", ""),
                    DataMetric("통증 부위", "-", ""),
                    DataMetric("스트레스", "-", ""),
                    DataMetric("흡연 여부", "-", ""),
                    DataMetric("음주", "-", ""),
                    DataMetric("수면 시간", "-", ""),
                    DataMetric("활동 수준", "-", ""),
                    DataMetric("카페인", "-", "")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    metricsToShow.forEach { metric ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .height(104.dp),
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

                                // show average only when provided
                                if (metric.average.isNotBlank()) {
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
@Preview(showBackground = true, showSystemUi = true, name = "MemberHomeRoute (full)")
@Composable
private fun MemberHomeRoutePreview() {
    // Provide one DataMetric per requested health item so preview matches runtime UI
    val dummyMetrics = listOf(
        DataMetric("키", "170cm", ""),
        DataMetric("몸무게", "70kg", ""),
        DataMetric("질환", "없음", ""),
        DataMetric("수면 상태", "양호", ""),
        DataMetric("복약 여부", "없음", ""),
        DataMetric("통증 부위", "없음", ""),
        DataMetric("스트레스", "보통", ""),
        DataMetric("흡연 여부", "비흡연", ""),
        DataMetric("음주", "가끔", ""),
        DataMetric("수면 시간", "7시간", ""),
        DataMetric("활동 수준", "보통", ""),
        DataMetric("카페인", "하루 1잔", "")
    )

    val today = java.time.LocalDate.now()
    val sampleReservations = listOf(
        com.livon.app.feature.member.reservation.ui.ReservationUi(
            id = "r1",
            date = today,
            className = "힐링 필라테스",
            coachName = "김코치",
            coachRole = "요가/필라테스",
            coachIntro = "유연성 중심 레슨",
            timeText = "16:40",
            classIntro = "저녁 힐링 타임",
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
            companyName = "회사이름",
            nickname = "테스트회원"
        )
    }
}
