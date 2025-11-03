package com.livon.app.feature.coach.auth.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenB
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun CoachInfoAffilliationScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {

    var intro by remember { mutableStateOf("") }

    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = onBack,
        bottomBar = {
            PrimaryButtonBottom(
                text = if (intro.isBlank()) "건너뛰기" else "다음",
                onClick = onNext
            )
        }
    ) {
        Spacer(Modifier.height(60.dp))

        RequirementText("어디에 소속되어 있으신가요?")

        Spacer(modifier = Modifier.height(120.dp))

        LivonTextField(
            value = intro,
            onValueChange = { intro = it },
            label = "",
            placeholder = "소속을 입력해주세요",
            maxLength = 10,     // ✅ 글자수 제한
            showCounter = true  // ✅ 카운터 표시
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewCoachInfoAffilliationScreen() = PreviewSurface {
    CoachInfoAffilliationScreen()
}
