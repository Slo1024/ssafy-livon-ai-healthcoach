package com.livon.app.feature.member.reservation.ui

import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    imageUrl: String? = null, // 서버에서 전달되는 이미지 URL 있으면 우선 사용
    navController: NavHostController? = null // optional nav controller to let HomeNavBar navigate directly
) {
    var showReserveDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            // 상태바 안전영역
            Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                TopBar(title = "상세 정보", onBack = onBack)
            }
        },
        bottomBar = {
            // QnA 화면과 동일한 하단 바 배치로 통일
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
            ) {
                PrimaryButtonBottom(
                    text = "예약 하기",
                    onClick = {
                        // show confirmation modal here; onConfirm will call onReserveClick
                        showReserveDialog = true
                    },
                    bottomMargin = 0.dp,
                    applyNavPadding = false,
                )

                HomeNavBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    currentRoute = null,
                    navController = navController,
                    onNavigate = { route ->
                        when (route) {
                            "home" -> onNavigateHome()
                            "mypage" -> onNavigateToMyPage()
                            "booking" -> onNavigateHome()
                            "reservations" -> onNavigateHome()
                            else -> Unit
                        }
                    }
                )
            }
        }
    ) { inner ->
        // 세로 스크롤 가능하도록 변경: preview와 실기기에서 동일하게 보이도록
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // 상단: 이미지 + 클래스명
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp)),
                        // 기본 이미지 리소스는 내부에서 크롭하지 않음
                    )
                }

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

            Spacer(Modifier.height(12.dp))

            // 하단: 클래스 소개 등
            SectionTitle()
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

            LabelText(label = "코치", fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))

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

            LabelText(label = "정보", fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))

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

            // 추가 여백을 주어 내용이 길어질 때도 아래 버튼과 겹치지 않도록 함
//            Spacer(Modifier.height(72.dp))
        }
    }

    // Full-screen reservation confirmation modal (same overlay as QnASubmit)
    if (showReserveDialog) {
        ReservationCompleteDialog(
            onDismiss = { showReserveDialog = false },
            onConfirm = {
                showReserveDialog = false
                onReserveClick()
            },
            onChangeHealthInfo = {
                // If user wants to change health info, navigate via provided navController or caller
                showReserveDialog = false
                try { navController?.let { /* let caller handle navigation by onReserveClick flow or separate handler */ } } catch (_: Throwable) {}
            }
        )
    }
}

/* -------------------- 재사용 소구성 -------------------- */

@Composable
private fun SectionTitle() {
    Text(
        text = "클래스 소개",
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
