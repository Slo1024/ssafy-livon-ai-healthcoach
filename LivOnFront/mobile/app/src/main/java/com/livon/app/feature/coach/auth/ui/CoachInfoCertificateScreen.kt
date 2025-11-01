// com/livon/app/feature/coach/auth/ui/CoachInfoCertificateScreen.kt
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
fun CoachInfoCertificateScreen() {
    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Spacer(Modifier.height(60.dp))
        RequirementText("자격증을 입력해 주세요")
        LivonTextField(value = "", onValueChange = {}, label = "자격증", placeholder = "예: PT 자격증")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachInfoCertificateScreen() = PreviewSurface { CoachInfoCertificateScreen() }


