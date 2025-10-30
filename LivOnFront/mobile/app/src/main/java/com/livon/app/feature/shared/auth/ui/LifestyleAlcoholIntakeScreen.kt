// com/livon/app/feature/shared/auth/ui/LifestyleAlcoholIntakeScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.text.CaptionText
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun LifestyleAlcoholIntakeScreen() {
    CommonSignUpScreenB(
        title = "생활습관 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        Column(modifier = Modifier.padding(start = 25.dp), horizontalAlignment = Alignment.Start) {
            Spacer(Modifier.height(2.dp))
            RequirementText("음주 빈도를 선택해 주세요")
            Spacer(Modifier.height(3.dp))
            CaptionText("하나를 선택해 주세요")
        }
        Text("음주 척도 SurveyOption (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLifestyleAlcoholIntakeScreen() = PreviewSurface { LifestyleAlcoholIntakeScreen() }


