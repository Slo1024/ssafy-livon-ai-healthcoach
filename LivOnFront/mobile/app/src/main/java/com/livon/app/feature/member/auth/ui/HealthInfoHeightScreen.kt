// com/livon/app/feature/shared/auth/ui/HealthInfoHeightScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun HealthInfoHeightScreen() {
    var height by remember { mutableStateOf("") }   // 🟢 상태는 화면 최상단에서
    val isNextEnabled = height.isNotBlank()   // ✅ 키가 입력되었는가?
    CommonSignUpScreenB(
        title = "건강 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(
            text = "다음",
            onClick = {},
            enabled = isNextEnabled      // ✅ 여기!
        ) }
    ) {
        RequirementText("키를 입력해 주세요")
        Spacer(Modifier.height(200.dp))

        LivonTextField(
            value = height,                           // ✅ 문자열 "height" → 변수 height
            onValueChange = { raw ->
                // 숫자만 허용 + 최대 3자리 (예: 150)
                height = raw.filter { it.isDigit() }.take(3)
            },
            label = "키",
            placeholder = "키를 입력해주세요",
            maxLength = 3,                            // ✅ 키(cm)는 보통 3자리면 충분
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHealthInfoHeightScreen() = PreviewSurface { HealthInfoHeightScreen() }
