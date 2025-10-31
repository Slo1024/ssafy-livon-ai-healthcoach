// com/livon/app/ui/component/button/ChoiceButtonCard.kt
package com.livon.app.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
//    modifier: Modifier = Modifier
    modifier: Modifier = Modifier.size(135.dp, 160.dp), // ✅ 기본값: 기존 크기 유지
    textStyle: TextStyle = MaterialTheme.typography.titleLarge.copy( // ✅ 기본 24sp(공통)
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
),
    textAlign: TextAlign = TextAlign.Center
){
    val shape = RoundedCornerShape(10.dp)
    val containerColor = if (selected) Main else Basic
    val contentColor = if (selected) Basic else Main
    val border = if (selected) null else BorderStroke(2.dp, Main)

    Card(
        onClick = onClick,
        shape = shape,
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier
//            .width(135.dp)
//            .height(160.dp)
    ) {
        Box(
//            modifier = Modifier.width(135.dp).height(160.dp),
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
//            Text(
//                text = text,
//                style = MaterialTheme.typography.titleLarge.copy(
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = contentColor,
//                    letterSpacing = 0.sp
//                )
                Text(
                    text = text,
                    textAlign = textAlign,
                    style = textStyle.copy(color = contentColor) // ✅ 색상은 선택 상태에 맞춰 덮어쓰기
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
