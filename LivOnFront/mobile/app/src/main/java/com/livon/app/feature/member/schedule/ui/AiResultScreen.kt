package com.livon.app.feature.member.schedule.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.ui.draw.clip

@Composable
fun AiResultScreen(
    memberName: String,
    counselingDateText: String,
    counselingName: String,
    aiSummary: String,
    onBack: () -> Unit
) {
    CommonScreenC(
        topBar = { TopBar(title = "AI 분석 결과", onBack = onBack) }
    ) {
        // ColumnScope receiver: CommonScreenC이 좌우 패딩을 적용하므로 내부에서는 추가 horizontal padding 불필요
        Spacer(modifier = Modifier.height(50.dp))

        // 날짜 + 상담명 (semibold 12)
        Text(
            text = "$counselingDateText  $counselingName",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(5.dp))

        // 소개 문구 (semibold 12)
        Text(
            text = "상담 내용을 기반으로 AI가 핵심 내용을 정리했습니다.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(40.dp))

        // AI 요약 박스: 가로는 패딩 제외 꽉 채움, 높이 약 300, radius 10, bg F5F7FA, border 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF5F7FA))
                .border(
                    BorderStroke(1.dp, Color(0xFFE0E6EB)),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단: 👤 이름 요약 (semibold 12)
                Text(
                    text = "👤 ${memberName} 요약",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 본문: medium 12
                Text(
                    text = aiSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 주의사항: main 색상, light 10
        Text(
            text = "이 내용은 AI가 상담 내용을 기반으로 자동 생성한 요약이며, 실제 전문 상담사의 해석과 다를 수 있습니다.",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Light
            )
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun AiResultScreenPreview() {
    LivonTheme {
        AiResultScreen(
            memberName = "김○○님",
            counselingDateText = "00월 00일 0000",
            counselingName = "상담 이름",
            aiSummary = "전체적으로 전보다 안정된 상태를 보이고 있으며, 꾸준한 수면 관리와 스트레스 완화가 긍정적인 변화를 이끌고 있습니다.",
            onBack = {}
        )
    }
}
