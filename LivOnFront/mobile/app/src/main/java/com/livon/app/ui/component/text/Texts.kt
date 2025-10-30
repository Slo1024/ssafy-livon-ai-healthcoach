// com/livon/app/ui/component/text/Texts.kt
package com.livon.app.ui.component.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.livon.app.ui.preview.PreviewSurface

/** 요구사항 텍스트: titleLarge + Medium + onBackground */

@Composable
fun Topic(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge.copy(
//            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    )
}


@Composable
fun RequirementText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    )
}

/** 캡션 텍스트: bodySmall + Medium + onSurfaceVariant */
@Composable
fun CaptionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "Requirement & Caption")
@Composable
private fun PreviewTexts() = PreviewSurface {
    Column {
        Topic("기저질환")
        RequirementText("이메일을 입력해주세요!")
        CaptionText("입력하신 정보는 공개되지 않습니다.")

    }
}
