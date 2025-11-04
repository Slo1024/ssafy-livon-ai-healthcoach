// com/livon/app/feature/shared/auth/ui/RoleSelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.component.button.ChoiceButtonCard
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme

private const val TAG = "ResModeSelect"

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
                onClick = { selected?.let { onComplete(it) } }
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "어떤 방식으로\n상담을 원하시나요?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(80.dp))

            val desiredBtnWidth = 150.dp
            val minBtnWidth = 110.dp
            val gap = 16.dp
            val density = LocalDensity.current

            // track parent width in dp
            var parentWidthDp by remember { mutableStateOf(0.dp) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { parentCoords ->
                        val parentWidthPx = parentCoords.size.width
                        val parentDp = with(density) { parentWidthPx.toDp() }
                        if (parentWidthDp != parentDp) {
                            parentWidthDp = parentDp
                            Log.d(TAG, "measured parentWidthDp=$parentWidthDp")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // compute button size based on available parent width
                val availablePer = remember(parentWidthDp) {
                    if (parentWidthDp <= 0.dp) desiredBtnWidth else ((parentWidthDp - gap) / 2f).coerceAtLeast(minBtnWidth)
                }

                // final btn width: min(desired, availablePer)
                val btnWidth = if (availablePer < desiredBtnWidth) availablePer else desiredBtnWidth
                val btnHeight = (btnWidth * 1.2f)

                // debug log
                LaunchedEffect(parentWidthDp, btnWidth, btnHeight) {
                    Log.d(TAG, "parentWidthDp=$parentWidthDp, btnWidth=$btnWidth, btnHeight=$btnHeight")
                }

                val btnSizeModifier = Modifier.requiredSize(btnWidth, btnHeight)
                val btnTextStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                )

                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = btnSizeModifier, contentAlignment = Alignment.Center) {
                        ChoiceButtonCard(
                            text = "개인\n상담",
                            selected = selected == "personal",
                            onClick = { selected = "personal" },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = btnTextStyle,
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(modifier = btnSizeModifier, contentAlignment = Alignment.Center) {
                        ChoiceButtonCard(
                            text = "그룹\n상담",
                            selected = selected == "group",
                            onClick = { selected = "group" },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = btnTextStyle,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true, name = "ReservationModeSelectScreen")
//@Composable
//private fun PreviewReservationModeSelectScreen() {
//    LivonTheme { ReservationModeSelectScreen() }
//}
