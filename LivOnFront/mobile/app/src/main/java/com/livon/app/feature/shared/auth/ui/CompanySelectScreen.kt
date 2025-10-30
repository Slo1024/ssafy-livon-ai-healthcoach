// com/livon/app/feature/shared/auth/ui/CompanySelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.CompanySelectCard
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.input.LivonTextField
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

@Composable
fun CompanySelectScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCompany by remember { mutableStateOf<String?>(null) }

    val companies = listOf("삼성전자", "무직")

    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "완료",
                enabled = selectedCompany != null,
                onClick = { /* TODO: Complete signup */ }
            )
        }
    ) {
        Column {
            Text(
                text = "사용자님은\n어디에 재직중이신가요?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "무직도 가능해요!",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(Spacing.DescToContent))
            LivonTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "회사 검색",
                placeholder = "회사명을 입력하세요"
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                companies.forEach { company ->
                    CompanySelectCard(
                        text = company,
                        selected = selectedCompany == company,
                        onClick = { selectedCompany = company }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompanySelectScreenPreview() {
    LivonTheme {
        CompanySelectScreen()
    }
}
