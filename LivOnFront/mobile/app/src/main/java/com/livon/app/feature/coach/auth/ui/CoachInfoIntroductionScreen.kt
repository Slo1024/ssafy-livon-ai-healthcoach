// com/livon/app/feature/coach/auth/ui/CoachInfoIntroductionScreen.kt
package com.livon.app.feature.coach.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenB
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun CoachInfoIntroductionScreen() {
    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "완료", onClick = {}) }
    ) {
        Text("자기소개 입력 라인 (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachInfoIntroductionScreen() = PreviewSurface { CoachInfoIntroductionScreen() }


