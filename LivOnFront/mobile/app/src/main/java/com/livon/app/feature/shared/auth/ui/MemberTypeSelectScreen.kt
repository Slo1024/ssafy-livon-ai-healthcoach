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
fun MemberTypeSelectScreen(
    modifier: Modifier = Modifier,
    onComplete: (mode: String) -> Unit = {}
) {
    var selected by remember { mutableStateOf<String?>(null) }

    CommonSignUpScreenA(
        topBar = { TopBar(title = "회원가입", onBack = {}) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "다음",
                enabled = selected != null,
                onClick = { /* TODO */ }
            )
        },
        modifier = modifier
    ) {
        // content 루트는 전체 높이
        Box(Modifier.fillMaxSize()) {
            // 1) TopBar 아래 80dp는 CommonSignUpScreenA가 이미 넣어줌
            //    여기선 타이틀만 위쪽에 붙여 두세요.
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
            ) {
                RequirementText("소속된 기업이 있으신가요?")
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
                        text = "일반",
                        selected = selected == "general",
                        onClick = { selected = "general" },
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
                        text = "기업",
                        selected = selected == "business",
                        onClick = { selected = "business" },
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
@Preview(showBackground = true, showSystemUi = true, name = "MemberTypeSelectScreen")
@Composable
private fun PreviewMemberTypeSelectScreen() {
    LivonTheme { MemberTypeSelectScreen() }
}
