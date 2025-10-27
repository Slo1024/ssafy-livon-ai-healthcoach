package com.livon.app.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 선택 토글 카드형 버튼
 * selected: bg=primary, text=onPrimary
 * unselected: bg=surface(=basic), text=primary, border=2dp primary
 * MinW≈135, MinH=160 (실제 배치는 weight로 반응형 권장)
 */
@Composable
fun ChoiceCardButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val colors = CardDefaults.cardColors(
        containerColor = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface
    )
    val border = if (selected) null
    else BorderStroke(2.dp, MaterialTheme.colorScheme.primary)

    Card(
        onClick = onClick,
        shape = shape,
        colors = colors,
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minWidth = 135.dp, minHeight = 160.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
            )
        }
    }
}
