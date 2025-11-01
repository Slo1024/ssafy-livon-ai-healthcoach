// com/livon/app/ui/component/button/SurveyOption.kt
package com.livon.app.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.theme.Basic
import com.livon.app.ui.theme.Border
import com.livon.app.ui.theme.BorderBlack
import com.livon.app.ui.theme.Main
import com.livon.app.ui.theme.Sub2
import com.livon.app.ui.theme.LivonTheme

/**
 * 설문 옵션 버튼
 *
 * 🎨 명세
 * ▪ 선택됨(Choice)
 *   - 텍스트: Medium 20, 색상 Basic(#FFFFFF)
 *   - 배경: Main(#4965F6)
 *   - Radius: 5dp
 *   - 크기: W220 x H50
 *
 * ▪ 비선택(Unchoice)
 *   - 텍스트: Medium 20, 색상 BorderBlack(#000000)
 *   - 배경: Sub2(#EDF2FC)
 *   - Border: 1dp, 색상 Border(#BABABA)
 *   - Radius: 5dp
 *   - 크기: W220 x H50
 */
@Composable
fun SurveyOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(5.dp)

    val bgColor = if (selected) Main else Sub2
    val textColor = if (selected) Basic else BorderBlack
    val borderStroke = if (selected) null else BorderStroke(0.5.dp, Border)

    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        border = borderStroke,
        modifier = modifier
            .width(250.dp)
            .height(50.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        )
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "SurveyOption - Choice / Unchoice")
@Composable
private fun PreviewSurveyOption() {
    LivonTheme {
        androidx.compose.foundation.layout.Column {
            SurveyOption(text = "운동함", selected = true, onClick = {})
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(8.dp))
            SurveyOption(text = "운동안함", selected = false, onClick = {})
        }
    }
}
