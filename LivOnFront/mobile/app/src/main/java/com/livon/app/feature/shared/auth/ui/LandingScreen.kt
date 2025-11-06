// kotlin
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.theme.Main
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsBottomHeight

@Composable
fun LandingScreen(
    onKakaoLogin: () -> Unit = {},
    onNaverLogin: () -> Unit = {},
    onEmailLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
    useBrandAsset: Boolean = true, // 기본값을 true로 변경 (공식 버튼 사용)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Main) // color.kt의 Main
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // 가운데 로고 텍스트를 약간 위로 올림
        Text(
            text = "LIVON",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-24).dp),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal // Regular
        )

        // 하단 버튼/링크 영역 (수직 패딩을 늘리고 추가 Spacer로 하단 여유 확보)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp), // vertical 증가
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KakaoLoginButton(
                onClick = onKakaoLogin,
                useBrandAsset = useBrandAsset
            )
            NaverLoginButton(
                onClick = onNaverLogin,
                useBrandAsset = useBrandAsset
            )

            Spacer(Modifier.height(8.dp))

            // 아래 링크들 (Bold 16, 밑줄)
            Text(
                text = "이메일로 로그인",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onEmailLogin)
            )
            Text(
                text = "회원가입",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onSignUp)
            )

            // 내비게이션 바 인셋 위에 추가 여유 공간
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

/* ---------------------------
   카카오 / 네이버 버튼
   - useBrandAsset=true: drawable 리소스 사용 (공식 버튼)
   - false: 임시(브랜드 컬러+텍스트) 버튼 (안전용)
   --------------------------- */

@Composable
private fun KakaoLoginButton(
    onClick: () -> Unit,
    useBrandAsset: Boolean
) {
    if (useBrandAsset) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.kakaobutton),
                contentDescription = "카카오 로그인",
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEE500),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "카카오 로그인",
                    color = Color(0xFF191919),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NaverLoginButton(
    onClick: () -> Unit,
    useBrandAsset: Boolean
) {
    if (useBrandAsset) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.naverbutton),
                contentDescription = "네이버 로그인",
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF03C75A),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "네이버 로그인",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Landing - Brand Assets")
@Composable
private fun LandingScreenPreview_BrandAssets() {
    LivonTheme {
        LandingScreen(
            onKakaoLogin = {},
            onNaverLogin = {},
            onEmailLogin = {},
            onSignUp = {}
            // useBrandAsset는 기본 true라 명시 불필요
        )
    }
}
