// 컴포넌트 확인 용

package com.livon.app.ui.component.streaming

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livon.app.feature.coach.streaming.ui.ConfigureUrlsScreen
import com.livon.app.feature.shared.streaming.ui.StreamingCheating
import com.livon.app.feature.shared.streaming.ui.StreamingParticipant
import com.livon.app.ui.theme.LivonTheme

@Preview(showBackground = true)
@Composable
fun PreviewHeaderBarComponent() {
    LivonTheme {
        // CoachStreamingHeader()
        // MemberStreamingHeader()
        // StreamingNav()
        // LiveStreamingCoachScreen()
        // ConfigureUrlsScreen()
        // StreamingParticipantsHeader(onBackClick = {})
        // StreamingCheatingHeader(onBackClick = {})
//        SearchBar() { }
        // CheatingBar() {}
//        CoachCheatingNotice(
//            message = "오늘은 훈련에 집중합시다!",
//            onDismissRequest = {},
//            onConfirm = {}
//        )
//        UserProfileItem(
//            userName = "참가자 A",
//        )
//        StreamingCheatingProfile(
//            userName = "홍길동 코치",
//            message = "오늘은 여기서 마무리하겠습니다",
//            time = "14:30"
//        )
//
//        val sampleBio = listOf(
//            BioInfo("고혈압"),
//            BioInfo("수면 질 저하"),
//            BioInfo("활동 부족"),
//            BioInfo("과체중"),
//        )
//
//        val sampleQA = listOf(
//            QAItem("전완근을 키우고 싶어요"),
//            QAItem("렛풀다운을 잘하고 싶어요"),
//        )
//
//        StreamingMemberDetail(
//            memberName = "홍길동",
//            bioInfoList = sampleBio,
//            bottomText = "혈압약 복용 중이므로 격렬한 운동은 피하세요",
//            qaList = sampleQA
//        )
//        StreamingParticipant(
//            onBackClick = { /* 뒤로가기 처리 */ },
//            onSearch = { query -> /* 검색 처리 */ }
//        )
        StreamingCheating(
            onBackClick = { /* 뒤로가기 처리 */ },
            onSearch = { query -> /* 검색 처리 */ }
        )
    }
}