package com.livon.app.ui.component.streaming

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.theme.*

@Composable
fun UserProfileItem(
    userName: String,
    modifier: Modifier = Modifier,
    profileImageResId: Int = R.drawable.profile
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(47.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = profileImageResId),
            contentDescription = "사용자 프로필",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(28.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = userName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            maxLines = 1
        )
    }
}