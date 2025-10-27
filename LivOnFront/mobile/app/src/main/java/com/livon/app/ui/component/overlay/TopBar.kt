package com.livon.app.ui.component.overlay

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 상단바 H40, back 아이콘, title 18 Bold borderBlack
 */
@Composable
fun LivonTopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp)
    ) {
        IconButton(onClick = { onBack?.invoke() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로")
        }
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000) // borderBlack
                ),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
    Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
}
