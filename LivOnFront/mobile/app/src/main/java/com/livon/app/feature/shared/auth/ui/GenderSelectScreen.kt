// com/livon/app/feature/shared/auth/ui/GenderSelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.button.SurveyOption
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

@Composable
fun GenderSelectScreen(
    modifier: Modifier = Modifier
) {
    var selectedGender by remember { mutableStateOf<String?>(null) }

    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = {},
        bottomBar = { 
            PrimaryButtonBottom(
                text = "다음",
                enabled = selectedGender != null,
                onClick = { /* TODO: Navigate to next screen */ }
            ) 
        }
    ) {
        Text(
            text = "성별을 선택해주세요.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "입력하신 정보는 공개되지 않습니다.\n코칭 맞춤 분석을 위해 필요합니다.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.DescToContent))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyOption(
                text = "남성",
                selected = selectedGender == "M",
                onClick = { selectedGender = "M" }
            )
            SurveyOption(
                text = "여성",
                selected = selectedGender == "F",
                onClick = { selectedGender = "F" }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenderSelectScreenPreview() {
    LivonTheme {
        GenderSelectScreen()
    }
}