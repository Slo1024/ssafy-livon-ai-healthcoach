// com/livon/app/feature/shared/auth/ui/SignUpCompleteScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun SignUpCompleteScreen() {
    // TopBar 없이 하단 PrimaryButton만
    Box(Modifier.fillMaxSize()) {
        PrimaryButtonBottom(text = "시작하기", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSignUpCompleteScreen() = PreviewSurface { SignUpCompleteScreen() }


