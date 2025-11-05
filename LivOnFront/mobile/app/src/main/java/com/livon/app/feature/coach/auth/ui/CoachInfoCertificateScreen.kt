// kotlin
package com.livon.app.feature.coach.auth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.feature.shared.auth.ui.CommonSignUpScreenB
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.preview.PreviewSurface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.livon.app.ui.theme.Gray2
import androidx.compose.foundation.layout.Arrangement

@Composable
fun CoachInfoCertificateScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    var input by remember { mutableStateOf("") }
    var certificates by remember { mutableStateOf(listOf<String>()) }

    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = onBack,
        bottomBar = {
            PrimaryButtonBottom(
                text = if (certificates.isEmpty()) "건너뛰기" else "다음",
                onClick = onNext
            )
        }
    ) {
        RequirementText("소지한 자격증이 있으신가요?")

        Spacer(Modifier.height(120.dp))

        // 추가된 자격증 리스트 — RequirementText와 입력 필드 사이에 표시
        CertificatesList(certificates)

        Spacer(Modifier.height(8.dp))

        // 입력 필드 (0/50)
        LivonTextField(
            value = input,
            onValueChange = { input = it },
            label = "자격증",
            placeholder = "자격증을 입력해주세요.",
            maxLength = 50,
            showCounter = true
        )

        Spacer(Modifier.height(8.dp))

        // 오른쪽 정렬된 '추가' 버튼 — '추가'는 regular 15sp, '+'는 더 큼(18sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .clickableNoRipple(enabled = input.isNotBlank()) {
                        certificates = certificates + input
                        input = ""
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "추가",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (input.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (input.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CertificatesList(certificates: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        certificates.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray2,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "${item.length}/50",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
            Divider()
        }
    }
}

/* ------- PREVIEWS ------- */

@Preview(showBackground = true, name = "No certificate added")
@Composable
private fun PreviewCoachInfoCertificateScreen_Empty() = PreviewSurface {
    CoachInfoCertificateScreen()
}

@Preview(showBackground = true, name = "Certificates added")
@Composable
private fun PreviewCoachInfoCertificateScreen_Filled() = PreviewSurface {
    var mockInput by remember { mutableStateOf("") }
    var mockList by remember { mutableStateOf(listOf("생활스포츠지도사 2급")) }

    CommonSignUpScreenB(
        title = "코치 정보 입력",
        onBack = {},
        bottomBar = { PrimaryButtonBottom(text = "다음", onClick = {}) }
    ) {
        RequirementText("소지한 자격증이 있으신가요?")

        Spacer(Modifier.height(120.dp))

        CertificatesList(mockList)

        Spacer(Modifier.height(8.dp))

        LivonTextField(
            value = mockInput,
            onValueChange = { mockInput = it },
            label = "자격증",
            placeholder = "자격증을 입력해주세요.",
            maxLength = 50,
            showCounter = true
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .clickableNoRipple(enabled = mockInput.isNotBlank()) {
                        mockList = mockList + mockInput
                        mockInput = ""
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "추가",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (mockInput.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (mockInput.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/* (선택) ripple 없는 클릭 확장함수: 필요 시 사용 */
private fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = if (enabled) {
    this.clickable(
        indication = null,
        interactionSource = MutableInteractionSource(),
        onClick = onClick
    )
} else this
