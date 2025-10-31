// app/src/main/java/com/livon/app/feature/member/reservation/ui/CoachListScreen.kt
package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.livon.app.R
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.*

/** 리스트용 간단 VO (실서버 연동 전 임시) */
data class CoachUi(
    val id: String,
    val name: String,
    val job: String?,
    val intro: String,
    val avatarUrl: String? // null이면 기본 아이콘(ic_noprofile) 사용
)

@Composable
fun CoachListScreen(
    onBack: () -> Unit = {},
    onSelectCoach: (CoachUi) -> Unit = {}
) {
    // TODO: 나중에 ViewModel/Repository 연동
    val allCoaches by remember {
        mutableStateOf(
            List(23) { idx ->
                CoachUi(
                    id = "coach-$idx",
                    name = "김코치$idx",
                    job = if (idx % 3 == 0) "피트니스 코치" else "영양 코치",
                    intro = "개인 맞춤형 프로그램으로 체형 교정과 컨디션 향상을 돕습니다.",
                    avatarUrl = if (idx % 4 == 0) null else "" // 대부분 빈 URL, 일부는 null로 기본 아이콘
                )
            }
        )
    }

    val pageSize = 10
    var currentPage by remember { mutableStateOf(1) }
    val totalPages = (allCoaches.size + pageSize - 1) / pageSize
    val pageItems = remember(allCoaches, currentPage) {
        val from = (currentPage - 1) * pageSize
        allCoaches.drop(from).take(pageSize)
    }

    Scaffold(
        topBar = { TopBar(title = "코치 선택", onBack = onBack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp) // 바깥 공통 마진
        ) {
            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(pageItems, key = { it.id }) { coach ->
                    CoachCard(
                        coach = coach,
                        onClick = { onSelectCoach(coach) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            PaginationBar(
                current = currentPage,
                total = totalPages,
                onChange = { currentPage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

/** 디자인 스펙 고정 카드 (327 x 153 / 가로는 꽉 채우고 높이 153dp 고정) */
@Composable
private fun CoachCard(
    coach: CoachUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameLetter = 0.08.sp  // 16sp * 0.5%
    val jobLetter = 0.04.sp   // 8sp  * 0.5%
    val introLetter = 0.065.sp // 13sp * 0.5%

    Card(
        onClick = onClick,
        modifier = modifier
            .height(153.dp), // 고정 높이
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(0.2.dp, Border), // 내부 보더
        colors = CardDefaults.cardColors(containerColor = Basic)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 텍스트 영역 (왼쪽)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 이름 + 직업 한 줄
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = coach.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Main,
                            fontSize = 16.sp,
                            letterSpacing = nameLetter
                        ),
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    coach.job?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Gray,
                                fontSize = 8.sp,
                                letterSpacing = jobLetter
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = coach.intro,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Gray2,
                        fontSize = 13.sp,
                        letterSpacing = introLetter
                    ),
                    maxLines = 3
                )
            }

            Spacer(Modifier.width(12.dp))

            // 아바타 (오른쪽, 기본 80 x 80)
            val noProfile = painterResource(R.drawable.ic_noprofile)
            if (!coach.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = coach.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    placeholder = noProfile,
                    error = noProfile
                )
            } else {
                Image(
                    painter = noProfile,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

/** 1..N 페이지 버튼 */
@Composable
private fun PaginationBar(
    current: Int,
    total: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (total <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이전
        TextButton(
            onClick = { if (current > 1) onChange(current - 1) },
            enabled = current > 1
        ) { Text("이전") }

        Spacer(Modifier.width(8.dp))

        // 번호
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (p in 1..total) {
                if (p == current) {
                    FilledTonalButton(onClick = { /* no-op */ }) {
                        Text("$p")
                    }
                } else {
                    OutlinedButton(onClick = { onChange(p) }) {
                        Text("$p")
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // 다음
        TextButton(
            onClick = { if (current < total) onChange(current + 1) },
            enabled = current < total
        ) { Text("다음") }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 720, name = "CoachListScreen")
@Composable
private fun PreviewCoachListScreen() = PreviewSurface {
    CoachListScreen()
}