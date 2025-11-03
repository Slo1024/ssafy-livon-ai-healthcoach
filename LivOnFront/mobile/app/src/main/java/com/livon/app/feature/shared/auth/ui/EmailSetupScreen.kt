// app/src/main/java/com/livon/app/feature/shared/auth/ui/EmailSetupScreen.kt
package com.livon.app.feature.shared.auth.ui

import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenA

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

@Composable
fun EmailSetupScreen(
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    val isValidEmail = email.contains("@") && email.contains(".")

    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = isValidEmail,
                onClick = { /* TODO: Navigate to next screen */ }
            )
        }
    ) {
        Text(
            text = "만나서 반가워요\n이메일을 입력해주세요!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(140.dp))

        LivonTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            placeholder = "example@example.com",
            maxLength = 30
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmailSetupScreenPreview_Empty() {
    LivonTheme {
        EmailSetupScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun EmailSetupScreenPreview_Valid() {
    LivonTheme {
        var email by remember { mutableStateOf("user@example.com") }
        val maxLength = 30
        val isValidEmail = email.contains("@") && email.contains(".")

        CommonSignUpScreenA(
            topBar = { TopBar(title = "회원가입", onBack = {}) },
            bottomBar = {
                PrimaryButtonBottom(
                    text = "다음",
                    enabled = isValidEmail,
                    onClick = {}
                )
            }
        ) {
            Text(
                text = "만나서 반가워요\n이메일을 입력해주세요!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(140.dp))
            LivonTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = { email = it },
                label = "이메일",
                placeholder = "example@example.com",
                maxLength = maxLength
            )
        }
    }
}
