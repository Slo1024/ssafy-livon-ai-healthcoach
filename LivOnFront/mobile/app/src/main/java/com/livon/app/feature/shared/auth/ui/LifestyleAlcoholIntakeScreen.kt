// com/livon/app/feature/shared/auth/ui/LifestyleAlcoholIntakeScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun LifestyleAlcoholIntakeScreen() {
    CommonSignUpScreenB(
        title = "생활습관 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Text("음주 척도 SurveyOption (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLifestyleAlcoholIntakeScreen() = PreviewSurface { LifestyleAlcoholIntakeScreen() }


