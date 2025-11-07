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
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNext: (String) -> Unit = {}
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // allow passwords of any length; require non-empty and matching confirmation
    val isValid = password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword

    CommonSignUpScreenA(
        modifier = modifier,
        topBar = { TopBar(title = "회원가입", onBack = onBack) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = isValid,
                onClick = { if (isValid) onNext(password) }
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
            placeholder = "비밀번호를 입력해주세요",
            maxLength = 64
        )
        Spacer(Modifier.height(12.dp))
        LivonTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "비밀번호 확인",
            placeholder = "비밀번호를 입력해주세요",
            maxLength = 64
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
        var password by remember { mutableStateOf("pw1") }
        var confirmPassword by remember { mutableStateOf("pw1") }
        val isValid = password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword

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
                placeholder = "비밀번호를 입력해주세요",
                maxLength = 64
            )
            Spacer(Modifier.height(12.dp))
            LivonTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "비밀번호 확인",
                placeholder = "비밀번호를 입력해주세요",
                maxLength = 64
            )
        }
    }
}