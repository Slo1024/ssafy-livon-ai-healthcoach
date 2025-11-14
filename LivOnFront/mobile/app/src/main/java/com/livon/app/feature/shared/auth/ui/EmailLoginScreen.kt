package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.component.button.PrimaryButtonCore
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.R
import com.livon.app.ui.theme.Gray
import com.livon.app.ui.theme.Gray2

@Composable
fun EmailLoginScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> },
    onSignUp: () -> Unit = {},
    onFindId: () -> Unit = {},
    onFindPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pwVisible by remember { mutableStateOf(false) }

    // 단순 검증
    val emailValid = email.contains("@") && email.contains(".")
    // allow passwords of any length (including < 8)
    val enabled = email.isNotBlank() && password.isNotBlank() && emailValid

    CommonSignUpScreenA(
        modifier = modifier,
        topBar = { TopBar(title = "이메일 로그인", onBack = onBack) },
        bottomBar = { } // no-op bottomBar

    ) {
        Text(
            text = "이메일로 로그인을 해주세요",
            style = MaterialTheme.typography.titleLarge.copy(
                color = Gray2,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(Modifier.height(140.dp))

        // 이메일
        LivonTextField(
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            placeholder = "example@example.com",
            maxLength = 30,
            showCounter = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            errorText = if (email.isNotEmpty() && !emailValid) "이메일 형식을 확인해주세요." else null
        )

        Spacer(Modifier.height(40.dp))

        // 비밀번호
        LivonTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            placeholder = "비밀번호를 입력해주세요.",
            showCounter = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation =
                if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingContent = {
                Icon(
                    painter = painterResource(
                        id = if (pwVisible) R.drawable.ic_visibility_off
                        else R.drawable.ic_visibility
                    ),
                    contentDescription = "비밀번호 보기",
                    tint = Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickableNoRipple { pwVisible = !pwVisible }
                )
            },
            // no minimum length error shown; allow any non-empty password
        )

        Spacer(Modifier.height(40.dp))

        // 로그인 버튼 — 비밀번호 입력과 하단 링크 사이에 위치
        PrimaryButtonCore(
            text = "로그인",
            enabled = enabled,
            onClick = { if (enabled) onLogin(email, password) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        Spacer(Modifier.height(12.dp))

        // 하단 링크
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LinkText("회원가입", onSignUp)
            SeparatorDot()
            LinkText("아이디 찾기", onFindId)
            SeparatorDot()
            LinkText("비밀번호 찾기", onFindPassword)
        }
    }
}

/* ---------- Utils ---------- */

@Composable
private fun LinkText(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.outline,
        fontSize = 12.sp,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickableNoRipple(onClick)
    )
}

@Composable
private fun SeparatorDot() {
    Text(
        text = "·",
        color = MaterialTheme.colorScheme.outline,
        fontSize = 12.sp
    )
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        indication = null,
        interactionSource = MutableInteractionSource(),
        onClick = onClick
    )

/* ---------- PREVIEW ---------- */

@Preview(showBackground = true)
@Composable
private fun PreviewEmailLogin_Disabled() = PreviewSurface {
    EmailLoginScreen()
}

@Preview(showBackground = true)
@Composable
private fun PreviewEmailLogin() = PreviewSurface {
    LivonTheme { EmailLoginScreen() }
}

