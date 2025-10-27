package com.livon.app.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 회사 선택 카드
 * before: bg=#F7F8FA, text=#000000
 * after : bg=basic(surface), text=#000000, stroke 2dp #7F7CFA(임시: secondary로 매핑)
 */
@Composable
fun CompanySelectCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val bgBefore = Color(0xFFF7F8FA)
    val borderSelected = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surface else bgBefore
        ),
        border = if (selected) borderSelected else null,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minWidth = 276.dp, minHeight = 153.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF000000),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal)
        )
    }
}
