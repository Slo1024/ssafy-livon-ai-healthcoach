// com/livon/app/feature/shared/auth/ui/HealthInfoPainDiscomfortScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun HealthInfoPainDiscomfortScreen() {
    CommonSignUpScreenB(
        title = "건강 상태 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Text("통증/불편함 SurveyOption (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHealthInfoPainDiscomfortScreen() = PreviewSurface { HealthInfoPainDiscomfortScreen() }


