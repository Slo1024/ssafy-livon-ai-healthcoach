// com/livon/app/feature/shared/auth/ui/RoleSelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.livon.app.ui.theme.Spacing
import androidx.compose.foundation.layout.height

@Composable
fun RoleSelectScreen(
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf<String?>(null) }

    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = selected != null,
                onClick = { /* TODO: navigate */ }
            )
        },
        modifier = modifier
    ) {
        Text(
            text = "어떤 포지션으로 참여하나요?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.DescToContent))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChoiceButtonCard(
                text = "회원",
                selected = selected == "member",
                onClick = { selected = "member" }
            )
            ChoiceButtonCard(
                text = "코치",
                selected = selected == "coach",
                onClick = { selected = "coach" }
            )
        }
    }
}

/* ---------- Previews ---------- */
@Preview(showBackground = true, name = "RoleSelectScreen - Unselected")
@Composable
private fun PreviewRoleSelectScreen_Unselected() {
    LivonTheme { RoleSelectScreen() }
}

@Preview(showBackground = true, name = "RoleSelectScreen - Member selected")
@Composable
private fun PreviewRoleSelectScreen_MemberSelected() {
    LivonTheme {
        var selected by remember { mutableStateOf("member") }
        CommonSignUpScreenA(
            topBar = { TopBar(title = "회원가입", onBack = {}) },
            bottomBar = {
                PrimaryButtonBottom(
                    text = "다음",
                    enabled = selected != null,
                    onClick = {}
                )
            }
        ) {
            Text(
                text = "어떤 포지션으로 참여하나요?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(Spacing.DescToContent))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChoiceButtonCard(
                    text = "회원",
                    selected = true,
                    onClick = { selected = "member" }
                )
                ChoiceButtonCard(
                    text = "코치",
                    selected = false,
                    onClick = { selected = "coach" }
                )
            }
        }
    }
}
