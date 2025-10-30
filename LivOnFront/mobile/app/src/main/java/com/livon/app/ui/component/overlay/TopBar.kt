// com/livon/app/ui/component/overlay/TopBar.kt
package com.livon.app.ui.component.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.theme.BorderBlack
import com.livon.app.ui.theme.LivonTheme

/**
 * 상단바 H40, 좌측 Back, 중앙 Title(18 Bold)
 * - back 아이콘: res/drawable/ic_back.xml
 */
@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BorderBlack
            )
        )
    }
    Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "TopBar")
@Composable
private fun PreviewTopBar() {
    LivonTheme {
        TopBar(title = "회원 로그인", onBack = {})
    }
}
