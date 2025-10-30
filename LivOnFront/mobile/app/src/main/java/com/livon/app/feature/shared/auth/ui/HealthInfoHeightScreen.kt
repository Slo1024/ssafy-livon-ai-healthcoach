// com/livon/app/feature/shared/auth/ui/HealthInfoHeightScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun HealthInfoHeightScreen() {
    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Text("키 입력 라인 (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHealthInfoHeightScreen() = PreviewSurface { HealthInfoHeightScreen() }


