package com.livon.app.feature.shared.auth.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.component.text.RequirementText
import com.livon.app.ui.component.text.CaptionText
import androidx.compose.foundation.Image
import com.livon.app.R

@Composable
fun ProfilePhotoSelectScreen(
    onBack: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    CommonSignUpScreenA(
        topBar = { TopBar(title = "프로필 설정", onBack = onBack) },
        bottomBar = {
            PrimaryButtonBottom(
                text = if (selectedImageUri == null) "건너뛰기" else "다음",
                onClick = onComplete
            )
        }
    ) {
        Spacer(Modifier.height(8.dp))

        RequirementText("왈라비 님의 프로필 이미지를\n등록해주세요.")
        Spacer(Modifier.height(6.dp))
        CaptionText("그룹 구성원들에게 알려줄 수도 있어요!")
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { picker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val painter = selectedImageUri?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(id = R.drawable.ic_noprofile)

                Image(
                    painter = painter,
                    contentDescription = "프로필 사진 선택",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // 아이콘만 보이도록, 내부로 오프셋(음수) 및 zIndex 적용
                Image(
                    painter = painterResource(id = R.drawable.pencil),
                    contentDescription = "프로필 편집",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-6).dp, y = (-6).dp)
                        .size(28.dp)
                        .zIndex(1f)
                        .clickable { picker.launch("image/*") },
                    contentScale = ContentScale.Fit,
                    colorFilter = null // or tint = Color.Unspecified
                )

            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfilePhotoSelectScreen() = PreviewSurface {
    ProfilePhotoSelectScreen()
}
