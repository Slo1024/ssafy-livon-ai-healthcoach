// com/livon/app/feature/shared/auth/ui/EmailLoginScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun EmailLoginScreen() {
    CommonSignUpScreenA(
        topBar = { TopBar(title = "이메일 로그인", onBack = {}) },
        bottomBar = { PrimaryButtonBottom(text = "로그인", onClick = {}) }
    ) {
        Text("이메일/비밀번호 입력 (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEmailLoginScreen() = PreviewSurface { EmailLoginScreen() }


