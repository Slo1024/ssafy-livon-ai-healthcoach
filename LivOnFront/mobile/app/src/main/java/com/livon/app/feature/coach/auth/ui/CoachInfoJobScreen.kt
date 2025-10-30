// com/livon/app/feature/coach/auth/ui/CoachInfoJobScreen.kt
package com.livon.app.feature.coach.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenB
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun CoachInfoJobScreen() {
    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Text("직무 입력 라인 (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachInfoJobScreen() = PreviewSurface { CoachInfoJobScreen() }


