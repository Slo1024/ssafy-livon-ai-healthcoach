// com/livon/app/feature/shared/auth/ui/HealthInfoWeightScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun HealthInfoWeightScreen() {
    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Text("몸무게 입력 라인 (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHealthInfoWeightScreen() = PreviewSurface { HealthInfoWeightScreen() }


