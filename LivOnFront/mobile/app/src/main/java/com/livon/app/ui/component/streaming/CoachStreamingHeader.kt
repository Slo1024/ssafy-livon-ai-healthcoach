package com.livon.app.ui.component.streaming

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.livon.app.R
import com.livon.app.ui.theme.*

@Composable
fun CoachStreamingHeader() {
    Surface(
        color = Basic,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomHeaderIcon(
                    iconResId = R.drawable.sound,
                    contentDescription = "사운드"
                )
                Spacer(Modifier.width(8.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomHeaderIcon(
                    iconResId = R.drawable.person,
                    contentDescription = "참가자"
                )
                Spacer(Modifier.width(8.dp))

                CustomHeaderIcon(
                    iconResId = R.drawable.chat,
                    contentDescription = "채팅"
                )
            }
        }
    }
}

@Composable
fun CustomHeaderIcon(@DrawableRes iconResId: Int, contentDescription: String, onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

