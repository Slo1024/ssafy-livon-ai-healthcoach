// com/livon/app/ui/component/overlay/TopBar2.kt
package com.livon.app.ui.component.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.livon.app.R
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Spacing

/**
 * H=70, 좌 Back, 중앙 Title (Medium 20, LetterSpacing 1%)
 * - back 아이콘: res/drawable/ic_back.xml
 */
@Composable
fun TopBar2(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(288.dp)
            .height(70.dp)
            .padding(horizontal = Spacing.Horizontal, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.01.em,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "TopBar2")
@Composable
private fun PreviewTopBar2() {
    LivonTheme {
        TopBar2(title = "건강 정보 입력", onBack = {})
    }
}
