// Kotlin
// 파일: `app/src/main/java/com/livon/app/feature/member/schedule/ui/AiResultScreen.kt`
package com.livon.app.feature.member.schedule.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.LivonTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.livon.app.R

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
        if (aiSummary.isBlank()) {
            // ai 분석 결과가 아직 없을 때: TopBar 유지, 본문은 로딩 아이콘 중앙 표시
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_loading),
                    contentDescription = "loading",
                    modifier = Modifier.size(64.dp)
                )
            }
        } else {
            // 기존 화면 (스크롤 가능)
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "$counselingDateText  $counselingName",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "상담 내용을 기반으로 AI가 핵심 내용을 정리했습니다.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "👤 $memberName 요약",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F7FA))
                        .border(
                            BorderStroke(1.dp, Color(0xFFE0E6EB)),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    val innerScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(innerScrollState)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = aiSummary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(90.dp))

                Text(
                    text = "이 내용은 AI가 상담 내용을 기반으로 자동 생성한 요약이며,\n실제 전문 상담사의 해석과 다를 수 있습니다.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiResultScreenPreviewEmpty() {
    LivonTheme {
        AiResultScreen(
            memberName = "김○○님",
            counselingDateText = "00월 00일 0000",
            counselingName = "상담 이름",
            aiSummary = "",
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AiResultScreenPreviewFilled() {
    LivonTheme {
        AiResultScreen(
            memberName = "김○○님",
            counselingDateText = "00월 00일 0000",
            counselingName = "상담 이름",
            aiSummary = "AI가 분석한 샘플 요약 텍스트입니다. 실제 데이터로 대체하세요.",
            onBack = {}
        )
    }
}