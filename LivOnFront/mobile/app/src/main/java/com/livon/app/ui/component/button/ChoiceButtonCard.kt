// com/livon/app/ui/component/button/ChoiceButtonCard.kt
package com.livon.app.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.theme.Basic
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Main

/**
 * ChoiceButtonCard
 *
 * ▪ Unchoice
 *   - 텍스트: Medium 24, 색상 Main
 *   - 배경: Basic(흰색)
 *   - 외곽선: 2dp, 색상 Main
 *   - Radius: 10dp
 *   - 크기: W=135, H=160
 *
 * ▪ Choice
 *   - 텍스트: Medium 24, 색상 Basic(흰색)
 *   - 배경: Main
 *   - 외곽선: 없음
 *   - Radius: 10dp
 *   - 크기: W=135, H=160
 */
@Composable
fun ChoiceButtonCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier, // 기본은 빈 Modifier로 두고 내부에서 필요시 적용
    textStyle: TextStyle = MaterialTheme.typography.titleLarge.copy( // ✅ 기본 24sp(공통)
        fontSize = 24.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    textAlign: TextAlign = TextAlign.Center
){
    val shape = RoundedCornerShape(10.dp)
    val containerColor = if (selected) Main else Basic
    val contentColor = if (selected) Basic else Main
    // Keep a constant stroke width to avoid layout shift; use transparent color when selected
    val border = BorderStroke(2.dp, if (selected) Color.Transparent else Main)

    // If caller passed an empty Modifier (default), apply the standard size; otherwise respect caller modifier
    val appliedModifier = if (modifier == Modifier) Modifier.size(135.dp, 160.dp) else modifier

    Card(
        onClick = onClick,
        shape = shape,
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = appliedModifier
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp) // 테두리 두께만큼 항상 내부 여백 확보
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                textAlign = textAlign,
                style = textStyle.copy(color = contentColor)
            )
        }
    }
}

/* ---------- Previews ---------- */
@Preview(showBackground = true, name = "ChoiceButtonCard - Unselected")
@Composable
private fun PreviewChoiceButtonCard_Unselected() {
    LivonTheme {
        ChoiceButtonCard(
            text = "회원",
            selected = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "ChoiceButtonCard - Selected")
@Composable
private fun PreviewChoiceButtonCard_Selected() {
    LivonTheme {
        ChoiceButtonCard(
            text = "코치",
            selected = true,
            onClick = {}
        )
    }
}
