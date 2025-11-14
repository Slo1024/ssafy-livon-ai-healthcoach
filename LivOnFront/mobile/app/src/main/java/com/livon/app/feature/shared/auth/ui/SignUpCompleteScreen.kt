// kotlin
// 변경 파일: app/src/main/java/com/livon/app/feature/shared/auth/ui/SignUpCompleteScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Gray2
import com.livon.app.ui.component.overlay.TopBar

@Composable
fun SignUpCompleteScreen(
    username: String, // 실제 등록된 이름을 전달
    onStart: () -> Unit = {}
) {
    // 이름에 '님'이 없으면 붙여서 표시
    val displayName = if (username.endsWith("님")) username else "${username}님"

    CommonSignUpScreenA(
        topBar = { TopBar(title = " ", onBack = {}) },   // 빈 텍스트 TopBar로 변경
        bottomBar = {
            PrimaryButtonBottom(
                text = "시작하기",
                onClick = onStart
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray2
                    )
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "환영합니다!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray2
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSignUpCompleteScreen() = PreviewSurface {
    SignUpCompleteScreen(username = "왈라비")
}

/*
 네비게이션에서 호출 예시 (NavHost 내부에서):
 val username = "등록된이름" // 실제로는 ViewModel 또는 이전 화면에서 받아온 값
 navController.navigate("signup_complete?username=${Uri.encode(username)}")

 composable route 예시:
 composable(
   route = "signup_complete?username={username}",
   arguments = listOf(navArgument("username") { type = NavType.StringType; defaultValue = "" })
 ) { backStackEntry ->
   val name = backStackEntry.arguments?.getString("username").orEmpty()
   SignUpCompleteScreen(username = name, onStart = { /* ... */ })
 }
*/

