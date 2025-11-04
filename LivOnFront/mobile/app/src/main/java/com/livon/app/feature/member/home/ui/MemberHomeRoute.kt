package com.livon.app.feature.member.home.ui

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
    metrics: List<DataMetric> = emptyList(),
    upcoming: List<UpcomingItem> = emptyList(),
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
                        BottomNavRoute.HOME.routeName -> { /* already on home */ }
                        BottomNavRoute.MY_PAGE.routeName -> { /* handle if needed */ }
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
                    text = "김싸피님",
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
                                    color = Color(0xFF555555)
                                )
                                Column {
                                    Text(
                                        text = metric.value,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
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
                                        letterSpacing = 0.01.em
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min) // 이 줄이 중요합니다
                ) {
                    // 전체 세로선 (리스트 전체에 걸쳐 이어짐)
                    Box(
                        modifier = Modifier
                            .offset(x = 18.dp) // dot의 가로 중심 위치
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFDDDDDD)) // 좀 더 명확한 회색
                            .align(Alignment.TopStart)
                    )

                    Column {
                        upcoming.forEach { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp) // 항목 간 간격
                            ) {
                                // 선과 정렬되는 영역: 너비 36dp, dot은 중앙에 위치
                                Box(
                                    modifier = Modifier
                                        .width(36.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .border(1.dp, Color.LightGray, CircleShape)
                                            .background(Color.White, CircleShape)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = item.datetime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

//            // 하단 내비 여유
//            Spacer(Modifier.height(88.dp))
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
            lineHeight = 16.sp,
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
    val dummyUpcoming = listOf(
        UpcomingItem("필라테스 클래스", "2025/11/15 14:00"),
        UpcomingItem("식단 코칭", "2025/11/16 19:30")
    )
    LivonTheme {
        MemberHomeRoute(
            onTapBooking = {},
            onTapReservations = {},
            metrics = dummyMetrics,
            upcoming = dummyUpcoming,
            companyName = "회사이름"
        )
    }
}
