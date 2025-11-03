// com/livon/app/ui/component/card/CoachCard.kt
package com.livon.app.ui.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.*

@Composable
fun CoachCard(
    name: String,
    job: String?,
    intro: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // 테두리를 "카드 내부"에 그리기 위한 두께(px) 계산
    val strokeDp = 0.2.dp

    Card(
        onClick = onClick,
        modifier = modifier,
//            .height(153.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Basic) // 흰 배경
    ) {
        // 내부에 직접 테두리를 그려서 "안쪽" 위치로 고정
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val strokePx = strokeDp.toPx()
                    // Stroke는 중심선 기준이므로, 정확히 내부에 보이게 inset 처리
                    val inset = strokePx / 2f
                    drawRect(
                        color = Border,
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - strokePx,
                            size.height - strokePx
                        ),
                        style = Stroke(width = strokePx)
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp), // ✅ 세로 패딩 없음
            ) {
                // 텍스트 영역
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // name + job 한 줄
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = Main,
                                fontSize = 16.sp,
                                letterSpacing = 0.08.sp // ≈ 0.5% 느낌
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.width(15.dp))

                        job?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Gray,
                                    fontSize = 8.sp,
                                    letterSpacing = 0.04.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = intro,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Gray2,
                            fontSize = 13.sp,
                            letterSpacing = 0.065.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(12.dp))

                // 아바타 (프로필 미등록 시 기본 아이콘)
                Image(
                    painter = painterResource(R.drawable.ic_noprofile),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "CoachCard")
@Composable
private fun PreviewCoachCard() = PreviewSurface {
    CoachCard(
        name = "김도윤",
        job = "피트니스 코치",
        intro = "유산소-근력 균형 트레이닝으로 체지방 감량과 근육 강화.\n1:1 상담 및 그룹 클래스 진행",
        avatarUrl = null,
        modifier = Modifier.fillMaxWidth()
    )
}
