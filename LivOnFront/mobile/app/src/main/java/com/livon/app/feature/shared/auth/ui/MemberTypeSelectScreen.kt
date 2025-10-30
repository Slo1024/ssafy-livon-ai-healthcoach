// com/livon/app/feature/shared/auth/ui/MemberTypeSelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.ChoiceButtonCard
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme

@Composable
fun MemberTypeSelectScreen(
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<String?>(null) }

    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = { 
            PrimaryButtonBottom(
                text = "완료",
                enabled = selectedType != null,
                onClick = { /* TODO: Complete signup */ }
            ) 
        }
    ) {
        Text(
            text = "소속된 기업이 있으신가요?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChoiceButtonCard(
                text = "일반",
                selected = selectedType == "general",
                onClick = { selectedType = "general" }
            )
            ChoiceButtonCard(
                text = "기업",
                selected = selectedType == "company",
                onClick = { selectedType = "company" }
            )
        }
    }
}

@Preview
@Composable
fun MemberTypeSelectScreenPreview() {
    LivonTheme {
        MemberTypeSelectScreen()
    }
}