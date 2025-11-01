// com/livon/app/feature/shared/auth/ui/TermOfUseScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun TermOfUseScreen() {
    CommonSignUpScreenA(
        topBar = { TopBar(title = "이용약관", onBack = {}) },
        bottomBar = { PrimaryButtonBottom(text = "동의하고 계속", onClick = {}) }
    ) {
        Text("약관 내용 (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTermOfUseScreen() = PreviewSurface { TermOfUseScreen() }


