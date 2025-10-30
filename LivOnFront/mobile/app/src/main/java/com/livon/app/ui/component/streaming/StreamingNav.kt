package com.livon.app.ui.component.streaming

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.theme.*
import com.livon.app.ui.theme.Pretendard

@Composable
fun StreamingNav() {
    Surface(
        color = Basic,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(vertical = 4.dp),

            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButtonWithText(
                iconResId = R.drawable.mic,
                text = "마이크",
                onClick = { /* 마이크 토글 로직 */ }
            )
            Spacer(Modifier.width(50.dp))

            NavButtonWithText(
                iconResId = R.drawable.video,
                text = "비디오",
                onClick = { /* 비디오 토글 로직 */ }
            )
            Spacer(Modifier.width(50.dp))

            NavButtonWithText(
                iconResId = R.drawable.share,
                text = "공유",
                onClick = { /* 화면 공유 로직 */ }
            )
            Spacer(Modifier.width(50.dp))

            NavButtonWithText(
                iconResId = R.drawable.more,
                text = "기능",
                onClick = { /* 더보기 메뉴 로직 */ }
            )
            Spacer(Modifier.width(50.dp))

            NavButtonWithText(
                iconResId = R.drawable.exit,
                text = "나가기",
                iconTint = Color.Red,
                onClick = { /* 나가기 로직 */ }
            )
        }
    }
}

@Composable
fun NavButtonWithText(
    @DrawableRes iconResId: Int,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Black,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = text,
            color = Color.Black,
            fontSize = 12.sp,
            maxLines = 1,
            fontFamily = Pretendard
        )
    }
}