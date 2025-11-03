package com.livon.app.feature.shared.auth.ui

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.CodeInputField
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import kotlinx.coroutines.delay

@Composable
fun EmailVerifyScreen(
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }
    var remainingSec by remember { mutableStateOf(180) } // 3분 = 180초

    // 카운트다운 루프
    LaunchedEffect(Unit) {
        while (remainingSec > 0) {
            delay(1000L)
            remainingSec -= 1
        }
    }

    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "인증하기",
                enabled = code.length == 4,
                onClick = { /* TODO: Verify code */ }
            )
        }
    ) {
        Text(
            text = "안전한 사용을 위해\n이메일 인증을 해주세요.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.alarmon),
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .padding(end = 8.dp),
            )
            Text(
                text = timeText,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(40.dp))

        CodeInputField(
            modifier = Modifier
                .fillMaxWidth(),
            code = code,
            onCodeChange = { code = it },
            onComplete = { /* TODO: Handle completion */ }
        )

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "인증번호 재발송",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        // TODO: resend logic
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmailVerifyScreenPreview_Empty() {
    LivonTheme {
        EmailVerifyScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun EmailVerifyScreenPreview_Partial() {
    LivonTheme {
        var code by remember { mutableStateOf("12") }

        CommonSignUpScreenA(
            topBar = { TopBar(title = "회원가입", onBack = {}) },
            bottomBar = {
                PrimaryButtonBottom(
                    text = "인증하기",
                    enabled = code.length == 4,
                    onClick = {}
                )
            }
        ) {
            Text(
                text = "안전한 사용을 위해\n이메일 인증을 해주세요.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            // preview용 고정 시간 표시
            Text(
                text = "03:00",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(70.dp))
            CodeInputField(
                code = code,
                onCodeChange = { code = it },
                onComplete = {}
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "인증번호 재발송",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmailVerifyScreenPreview_Full() {
    LivonTheme {
        var code by remember { mutableStateOf("1234") }

        CommonSignUpScreenA(
            topBar = { TopBar(title = "회원가입", onBack = {}) },
            bottomBar = {
                PrimaryButtonBottom(
                    text = "인증하기",
                    enabled = code.length == 4,
                    onClick = {}
                )
            }
        ) {
            Text(
                text = "안전한 사용을 위해\n이메일 인증을 해주세요.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "03:00",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(70.dp))
            CodeInputField(
                code = code,
                onCodeChange = { code = it },
                onComplete = {}
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "인증번호 재발송",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(start = 20.dp)
            )
        }
    }
}
