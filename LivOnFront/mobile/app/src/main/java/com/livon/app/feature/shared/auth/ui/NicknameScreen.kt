// com/livon/app/feature/shared/auth/ui/NicknameScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

@Composable
fun NicknameScreen(
    modifier: Modifier = Modifier
) {
    var nickname by remember { mutableStateOf("") }
    val isValid = nickname.length in 1..10

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
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "사용자님을\n어떻게 불러드리면 될까요?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(Spacing.DescToContent))
                LivonTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = "닉네임",
                    placeholder = "닉네임을 입력해주세요"
                )
            }
            Text(
                text = "${nickname.length}/10",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NicknameScreenPreview_Empty() {
    LivonTheme {
        NicknameScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun NicknameScreenPreview_Filled() {
    LivonTheme {
        var nickname by remember { mutableStateOf("Jinny") }
        val isValid = nickname.length in 1..10

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
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "사용자님을\n어떻게 불러드리면 될까요?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(Spacing.DescToContent))
                    LivonTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = "닉네임",
                        placeholder = "닉네임을 입력해주세요"
                    )
                }
                Text(
                    text = "${nickname.length}/10",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}