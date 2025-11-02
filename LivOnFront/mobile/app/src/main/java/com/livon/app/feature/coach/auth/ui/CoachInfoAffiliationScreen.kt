// com/livon/app/feature/coach/auth/ui/CoachInfoAffiliationScreen.kt
package com.livon.app.feature.coach.auth.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun CoachInfoAffiliationScreen() {
    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Spacer(Modifier.height(60.dp))
        RequirementText("소속을 입력해 주세요")
        Spacer(Modifier.height(120.dp))
        LivonTextField(
            value = "",
            onValueChange = {},
            label = "소속",
            placeholder = "회사/기관명",
            modifier = Modifier.padding(start = 0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCoachInfoAffiliationScreen() = PreviewSurface { CoachInfoAffiliationScreen() }


