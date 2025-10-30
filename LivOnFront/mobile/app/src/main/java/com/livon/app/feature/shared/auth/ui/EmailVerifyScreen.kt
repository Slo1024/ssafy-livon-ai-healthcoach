// com/livon/app/feature/shared/auth/ui/EmailVerifyScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.CodeInputField
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme

@Composable
fun EmailVerifyScreen(
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }

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
        Spacer(Modifier.height(20.dp))
        CodeInputField(
            code = code,
            onCodeChange = { code = it },
            onComplete = { /* TODO: Handle completion */ }
        )
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
            Spacer(Modifier.height(20.dp))
            CodeInputField(
                code = code,
                onCodeChange = { code = it },
                onComplete = {}
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
            Spacer(Modifier.height(20.dp))
            CodeInputField(
                code = code,
                onCodeChange = { code = it },
                onComplete = {}
            )
        }
    }
}