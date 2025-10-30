// com/livon/app/feature/shared/auth/ui/LifestyleSmokingScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun LifestyleSmokingScreen() {
    CommonSignUpScreenB(
        title = "생활습관 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Text("흡연 여부 SurveyOption (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLifestyleSmokingScreen() = PreviewSurface { LifestyleSmokingScreen() }


