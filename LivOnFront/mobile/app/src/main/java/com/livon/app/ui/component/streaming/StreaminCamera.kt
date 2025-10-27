package com.livon.app.ui.component.streaming

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.livon.app.R
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.aspectRatio

@Composable
fun StreamingCamera(
    userName: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int = R.drawable.profile
) {
    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            color = Color.DarkGray,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(327f / 539f)
                .align(Alignment.Center)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = "카메라 비활성화",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(72.dp)
                )

                UserNameTag(
                    name = userName,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun UserNameTag(name: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = name,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}