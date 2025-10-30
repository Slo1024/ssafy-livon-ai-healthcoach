// com/livon/app/feature/shared/auth/ui/ProfilePhotoSelectScreen.kt
package com.livon.app.feature.shared.auth.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface

@Composable
fun ProfilePhotoSelectScreen() {
    CommonSignUpScreenA(
        topBar = { TopBar(title = "프로필 설정", onBack = {}) },
        bottomBar = { PrimaryButtonBottom(text = "완료", onClick = {}) }
    ) {
        Text("프로필 사진 선택 영역 (placeholder)")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfilePhotoSelectScreen() = PreviewSurface { ProfilePhotoSelectScreen() }


