// com/livon/app/feature/shared/auth/ui/PasswordSetupScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

@Composable
fun PasswordSetupScreen(
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isValid = password.length >= 8 && confirmPassword.length >= 8 && password == confirmPassword

    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = { 
            PrimaryButtonBottom(
                text = "다음",
                enabled = isValid,
                onClick = { /* TODO: Navigate to next screen */ }
            ) 
        }
    ) {
        Text(
            text = "안전한 사용을 위해\n비밀번호를 입력해주세요.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.DescToContent))
        LivonTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            placeholder = "비밀번호를 입력해주세요"
        )
        Spacer(Modifier.height(12.dp))
        LivonTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "비밀번호 확인",
            placeholder = "비밀번호를 입력해주세요"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordSetupScreenPreview_Invalid() {
    LivonTheme {
        PasswordSetupScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordSetupScreenPreview_Valid() {
    LivonTheme {
        var password by remember { mutableStateOf("password123") }
        var confirmPassword by remember { mutableStateOf("password123") }
        val isValid = password.length >= 8 && confirmPassword.length >= 8 && password == confirmPassword

        CommonSignUpScreenA(
            topBar = { TopBar(title = "회원가입", onBack = {}) },
            bottomBar = { 
                PrimaryButtonBottom(
                    text = "다음",
                    enabled = isValid,
                    onClick = {}
                ) 
            }
        ) {
            Text(
                text = "안전한 사용을 위해\n비밀번호를 입력해주세요.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(Spacing.DescToContent))
            LivonTextField(
                value = password,
                onValueChange = { password = it },
                label = "비밀번호",
                placeholder = "비밀번호를 입력해주세요"
            )
            Spacer(Modifier.height(12.dp))
            LivonTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "비밀번호 확인",
                placeholder = "비밀번호를 입력해주세요"
            )
        }
    }
}