// com/livon/app/feature/shared/auth/ui/HealthInfoSleepQualityScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun HealthInfoSleepQualityScreen() {
    CommonSignUpScreenB(
        title = "건강 상태 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Text("수면상태 SurveyOption (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHealthInfoSleepQualityScreen() = PreviewSurface { HealthInfoSleepQualityScreen() }


