package com.livon.app.ui.component.streaming

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.livon.app.R
import com.livon.app.ui.theme.*

@Composable
fun StreamingCheatingProfile(
    userName: String,
    message: String,
    time: String,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    profileImageResId: Int = R.drawable.profile,
    role: String = "MEMBER"
) {
    val isCoach = role.equals("COACH", ignoreCase = true)
    val imageModifier = Modifier
        .padding(start = 4.dp)
        .size(50.dp)
        .clip(CircleShape)
        .then(
            if (isCoach)
                Modifier.border(3.dp, Main, CircleShape)
            else Modifier
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (profileImageUrl != null && profileImageUrl.isNotBlank()) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "사용자 프로필",
                contentScale = ContentScale.Crop,
                modifier = imageModifier,
                error = painterResource(id = profileImageResId),
                placeholder = painterResource(id = profileImageResId)
            )
        } else {
            Image(
                painter = painterResource(id = profileImageResId),
                contentDescription = "사용자 프로필",
                modifier = imageModifier
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCoach) Main else Color.Black
                )

                // 코치 배지 (이름 옆에)
                if (isCoach) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = Main.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "COACH",
                            color = Main,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = message,
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStreamingCheatingProfileCoach() {
    StreamingCheatingProfile(
        userName = "코치 김명주",
        message = "안녕하세요! 오늘은 피드백 드릴게요 👋",
        time = "오후 3:30",
        role = "COACH" // 🔹 COACH로 지정
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewStreamingCheatingProfileMember() {
    StreamingCheatingProfile(
        userName = "참가자 박현수",
        message = "안녕하세요, 잘 부탁드려요!",
        time = "오후 3:31",
        role = "MEMBER" // 🔹 MEMBER로 지정
    )
}
