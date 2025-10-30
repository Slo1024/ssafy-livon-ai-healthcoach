// com/livon/app/feature/shared/auth/ui/LifestyleSleepDurationScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun LifestyleSleepDurationScreen() {
    CommonSignUpScreenB(
        title = "생활습관 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        RequirementText("하루 평균 수면시간을 선택해 주세요")
        Spacer(Modifier.height(3.dp))
        CaptionText("항상 활성화")
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("시간 Picker (placeholder)")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLifestyleSleepDurationScreen() = PreviewSurface { LifestyleSleepDurationScreen() }


