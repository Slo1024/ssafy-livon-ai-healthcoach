// 컴포넌트 확인 용

package com.livon.app.ui.component.streaming

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.ui.theme.LivonTheme

@Preview(showBackground = true)
@Composable
fun PreviewHeaderBarComponent() {
    LivonTheme {
        // CoachStreamingHeader()
        // MemberStreamingHeader()
        // StreamingNav()
        StreamingCamera(
            userName = "사용자 A",
            modifier = Modifier.height(300.dp)
        )
    }
}