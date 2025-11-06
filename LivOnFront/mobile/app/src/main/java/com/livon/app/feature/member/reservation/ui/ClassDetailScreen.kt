package com.livon.app.feature.member.reservation.ui

import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.livon.app.R
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.navbar.HomeNavBar
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme

/**
 * 클래스 상세 화면 (캘린더/시간 선택 없음)
 * - 상단: TopBar("상세 정보")
 * - 본문: 이미지 + 클래스명(중앙정렬), 섹션(클래스 소개 / 코치 / 정보)
 * - 하단: 예약 하기 버튼 + HomeNavBar (서로 붙음)
 */
@Composable
fun ClassDetailScreen(
    className: String,
    coachName: String,
    classInfo: String,
    onBack: () -> Unit,
    onReserveClick: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    imageResId: Int = R.drawable.ic_classphoto, // 샘플: 실제에선 이미지 리소스/URL 연동
    navController: NavHostController? = null // optional nav controller to let HomeNavBar navigate directly
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            // 상태바 안전영역
            Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                TopBar(title = "상세 정보", onBack = onBack)
            }
        },
        bottomBar = {
            // 제스처바/IME 안전영역은 하단 레이아웃에서만 적용
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
            ) {
                PrimaryButtonBottom(
                    text = "예약 하기",
                    onClick = onReserveClick,
                    bottomMargin = 0.dp,        // 버튼과 내비바가 붙도록
                    applyNavPadding = false      // 내부 navPadding 비활성화(중복 방지)
                )
                HomeNavBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    currentRoute = null,
                    navController = navController,
                    onNavigate = { route ->
                        // Fallback behavior when navController not provided
                        when (route) {
                            "home" -> onNavigateHome()
                            "mypage" -> onNavigateToMyPage()
                        }
                    }
                )
            }
        }
    ) { inner ->
        // 루트로 Column 사용하여 inner padding 적용 및 weight 사용 가능하도록 함
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp) // 좌우 마진을 24.dp로 고정
        ) {
            // 상단: 이미지 + 클래스명 (화면의 60% 차지)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = className,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 하단: 클래스 소개 등 (남은 40% 차지, 아래로 내려감)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.height(8.dp))

                SectionTitle(text = "클래스 소개")
                Spacer(Modifier.height(10.dp))
                // 클래스 소개 본문: SemiBold 16, 회색
                Text(
                    text = classInfo,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(Modifier.height(20.dp))

                // 코치 레이블 (SemiBold 14)
                LabelText(label = "코치", fontSize = 14.sp)

                Spacer(Modifier.height(10.dp))

                // 코치 이름 값 (SemiBold 14, Gray)
                Text(
                    text = coachName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // 정보 레이블 (SemiBold 14)
                LabelText(label = "정보", fontSize = 14.sp)

                Spacer(Modifier.height(10.dp))

                // 정보 값 (SemiBold 14, Gray)
                Text(
                    text = classInfo,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/* -------------------- 재사용 소구성 -------------------- */

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

@Composable
private fun LabelText(label: String, fontSize: TextUnit = 13.sp) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

/* -------------------- Preview -------------------- */

@Preview(showBackground = true, name = "클래스 상세(샘플)")
@Composable
private fun ClassDetailScreenPreview() {
    LivonTheme {
        ClassDetailScreen(
            className = "필라테스 클래스",
            coachName = "김싸피",
            classInfo = "이 클래스는 스트레칭, 운동 자세 교정, 식단 관리를 코칭해주는 클래스 입니다.",
            onBack = {},
            onReserveClick = {},
            onNavigateHome = {},
            onNavigateToMyPage = {},
            imageResId = R.drawable.ic_classphoto // 없다면 임의 리소스로 교체
        )
    }
}
