// com/livon/app/feature/member/my/MyPageScreen.kt
package com.livon.app.feature.member.my

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.livon.app.R
import com.livon.app.feature.member.reservation.ui.ReservationCompleteDialog
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    userName: String? = "김싸피",
    profileImageUri: Uri? = null,
    onBack: () -> Unit = {},
    onClickHealthInfo: () -> Unit = {},
    onClickFaq: () -> Unit = {},
    onLogoutConfirm: () -> Unit = {}
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    // ✅ 로그아웃 모달 노출 여부
    var showLogoutDialog by remember { mutableStateOf(false) }

    CommonScreenC(
        modifier = modifier,
        topBar = { TopBar(title = "마이페이지", onBack = onBack) }
    ) {
        Spacer(Modifier.height(100.dp))

        // --- 프로필 영역 ---
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clickable { picker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Draw the circular border as a background layer so it stays behind the image and pencil
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(0.8.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )

                // Priority: user-picked image > profileImageUri from server > ic_noprofile
                val painter = selectedImageUri?.let { rememberAsyncImagePainter(it) }
                    ?: profileImageUri?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(id = R.drawable.ic_noprofile)

                Image(
                    painter = painter,
                    contentDescription = "프로필 사진 선택",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // 편집 아이콘(연필) - 안쪽 하단 오른쪽에 겹치기
                Image(
                    painter = painterResource(id = R.drawable.pencil),
                    contentDescription = "프로필 편집",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (4).dp, y = (4).dp)
                        .size(30.dp)
                        .zIndex(2f)
                        .clickable { picker.launch("image/*") },
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = userName ?: "회원님",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(Modifier.height(40.dp))

        // 풀-블리드 구분선
        FullWidthDivider()

        // --- 섹션: 내 정보 ---
        SectionHeader(text = "내 정보")
        Spacer(Modifier.height(32.dp))
        SettingRow(
            text = "나의 건강 정보",
            onClick = onClickHealthInfo
        )
        // 아래 여백
        Spacer(Modifier.height(72.dp))

        // 풀-블리드 구분선
        FullWidthDivider()

        // --- 섹션: 지원 ---
        SectionHeader(text = "지원")
        Spacer(Modifier.height(32.dp))
        SettingRow(
            text = "자주 묻는 질문",
            onClick = onClickFaq
        )

        // 아래 여백
        Spacer(Modifier.height(40.dp))

        // ✅ 로그아웃 버튼 (나의 건강 정보 / 자주 묻는 질문과 같은 형태)
        SettingRow(
            text = "로그아웃",
            onClick = { showLogoutDialog = true }
        )

        Spacer(Modifier.height(72.dp))
    }

    // ✅ 로그아웃 모달 (MyInfoScreen에서 쓰던 회색 배경 + 카드 그대로 사용)
    if (showLogoutDialog) {
        ReservationCompleteDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogoutConfirm() // 실제 로그아웃 로직은 바깥에서 주입
            },
            titleText = "로그아웃 하시겠습니까?",
            subtitleText = null,          // 🔹 설명 텍스트 없음
            showCancelButton = true,      // 🔹 [취소 | 확인] 두 개 버튼
            confirmLabel = "확인",
            cancelLabel = "취소"
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingRow(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        // ▶ 아이콘: ic_back을 좌우 반전해서 사용
        Image(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = -1f   // 좌우 반전으로 '>' 모양처럼 보이게
                }
        )
    }
}

@Composable
private fun FullWidthDivider() {
    // CommonScreenC의 가로 패딩(Spacing.Horizontal)을 고려하여
    // Divider를 가로로 꽉 차게 보이도록 외곽으로 빼서 그립니다.
    Box(modifier = Modifier.fillMaxWidth()) {
        // 내부에 그냥 Divider만 두면 충분 (Column이 이미 fullWidth)
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "MyPageScreen")
@Composable
private fun PreviewMyPageScreen() = PreviewSurface {
    MyPageScreen()
}
