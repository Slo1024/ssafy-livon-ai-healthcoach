// com/livon/app/feature/shared/auth/ui/HealthInfoWeightScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun HealthInfoWeightScreen() {
    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        RequirementText("몸무게를 입력해 주세요")
        Spacer(Modifier.height(105.dp))
        LivonTextField(
            value = "",
            onValueChange = {},
            label = "몸무게",
            placeholder = "예: 70",
            modifier = Modifier.padding(start = 25.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHealthInfoWeightScreen() = PreviewSurface { HealthInfoWeightScreen() }


