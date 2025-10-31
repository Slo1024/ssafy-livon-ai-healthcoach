// com/livon/app/feature/shared/auth/ui/RoleSelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.component.button.ChoiceButtonCard
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

@Composable
fun ReservationModeSelectScreen(
    modifier: Modifier = Modifier,
    onComplete: (mode: String) -> Unit = {}
) {
    var selected by remember { mutableStateOf<String?>(null) }

    CommonSignUpScreenA(
        topBar = { TopBar(title = "예약하기", onBack = {}) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "완료",
                enabled = selected != null,
                onClick = { /* TODO */ }
            )
        },
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
            ) {
                RequirementText("어떤 포지션으로 참여하나요?")
            }

            // 2) 화면(TopBar~BottomBar 사이) 전체 기준 '진짜 중앙'에 카드 2개
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .offset(y = (-40).dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ChoiceButtonCard(
                        text = "개인\n상담",
                        selected = selected == "personal",
                        onClick = { selected = "personal" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(135f / 160f) // 비율 유지
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ChoiceButtonCard(
                        text = "그룹\n상담",
                        selected = selected == "group",
                        onClick = { selected = "group" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(135f / 160f) // 비율 유지
                    )
                }
            }
        }
    }
}

/* ---------- Previews ---------- */
@Preview(showBackground = true, showSystemUi = true, name = "ReservationModeSelectScreen")
@Composable
private fun PreviewReservationModeSelectScreen() {
    LivonTheme { ReservationModeSelectScreen() }
}
