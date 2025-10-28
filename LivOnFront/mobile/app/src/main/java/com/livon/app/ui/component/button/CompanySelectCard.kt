// com/livon/app/ui/component/button/CompanySelectCard.kt
package com.livon.app.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.theme.Basic
import com.livon.app.ui.theme.BorderBlack
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Main

/**
 * 회사 선택 버튼
 * - W 276 x H 53, R10
 * - 텍스트: Regular 18, letterSpacing 0, 좌패딩 20
 * - 활성화: bg=Basic(white), border=Main 2dp
 * - 비활성: bg=#F7F8FA, border 없음
 */
@Composable
fun CompanySelectCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val inactiveBg = Color(0xFFF7F8FA)
    val container = if (selected) Basic else inactiveBg
    val borderStroke = if (selected) BorderStroke(2.dp, Main) else null

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = container),
        border = borderStroke,
        modifier = modifier
            .width(276.dp)
            .height(53.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(53.dp)
                .padding(start = 20.dp) // 텍스트 왼쪽 패딩 20
        ) {
            Text(
                text = text,
                color = BorderBlack,
                // Regular 18 + 자간 0
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "CompanySelectCard - Selected/Unselected")
@Composable
private fun PreviewCompanySelectCard() {
    LivonTheme {
        androidx.compose.foundation.layout.Column {
            CompanySelectCard(text = "삼성전자", selected = true, onClick = {})
            androidx.compose.foundation.layout.Spacer(Modifier.padding(6.dp))
            CompanySelectCard(text = "무직", selected = false, onClick = {})
        }
    }
}
