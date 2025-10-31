// com/livon/app/feature/coach/auth/ui/CoachInfoJobScreen.kt
package com.livon.app.feature.coach.auth.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenB
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun CoachInfoJobScreen() {
    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Spacer(Modifier.height(60.dp))
        RequirementText("직무를 입력해 주세요")
        LivonTextField(value = "", onValueChange = {}, label = "직무", placeholder = "예: 피트니스 코치")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachInfoJobScreen() = PreviewSurface { CoachInfoJobScreen() }


