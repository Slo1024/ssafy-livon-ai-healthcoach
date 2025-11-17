package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.livon.app.R
import com.livon.app.ui.theme.LivonTheme
import java.time.LocalDate

// ✅ 여기에서만 선언 (다른 파일에서 중복 선언 금지)
data class SampleClassInfo(
    val id: String,
    val coachId: String,
    val date: LocalDate,
    val time: String,
    val type: String, // "기업 클래스" or "일반 클래스"
    val imageUrl: String?,
    val className: String,
    val coachName: String,
    val description: String,
    val currentParticipants: Int,
    val maxParticipants: Int
)

// ... (다른 코드는 그대로 유지)

@Composable
fun ClassCard(
    classInfo: SampleClassInfo,
    onCardClick: () -> Unit,
    onCoachClick: () -> Unit,
    enabled: Boolean = true // 만원이거나 이미 예약한 경우 false
) {
    val isClosed = classInfo.currentParticipants >= classInfo.maxParticipants
    val remainingCount = classInfo.maxParticipants - classInfo.currentParticipants
    val participantsColor = if (isClosed || !enabled) Color.Gray else MaterialTheme.colorScheme.primary
    
    // [수정] 비활성화 시 시각적 표시를 위한 alpha 값
    val alpha = if (enabled) 1f else 0.6f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onCardClick)
            .padding(vertical = 12.dp) // 위아래에 패딩을 줍니다. (기존 Card의 내부 패딩과 유사한 효과)
            .graphicsLayer(alpha = alpha) // [수정] 비활성화 시 투명도 적용
    ) {
        // 상단 정보: 날짜, 시간, 클래스 유형
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp) // 좌우 패딩 추가
        ) {
            Text(classInfo.date.toString(), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(classInfo.time, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Text(
                classInfo.type,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
        Spacer(Modifier.height(8.dp))

        // 중앙 정보: 사진, 클래스명, 소개 등
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 12.dp) // 좌우 패딩 추가
        ) {
            // 이미지: imageUrl이 있으면 AsyncImage로 로드 (centerCrop), 없으면 기본 리소스 사용
            if (!classInfo.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = classInfo.imageUrl,
                    contentDescription = "클래스 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    placeholder = painterResource(id = R.drawable.ic_classphoto),
                    error = painterResource(id = R.drawable.ic_classphoto)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_classphoto),
                    contentDescription = "클래스 이미지",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    classInfo.className,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    letterSpacing = (0.01).sp
                )
                Text(
                    classInfo.coachName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    classInfo.description,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.height(12.dp)) // 대신 고정된 간격을 줍니다.

        // 하단 정보: 인원, 코치 보기 버튼
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp) // 좌우 패딩 추가
        ) {
            Icon(Icons.Default.Person, contentDescription = "참여 인원", tint = participantsColor)
            Spacer(Modifier.width(4.dp))
            val participantsText = if (isClosed || !enabled) {
                if (!enabled && !isClosed) {
                    "${classInfo.currentParticipants}/${classInfo.maxParticipants} (이미 예약됨)"
                } else {
                    "${classInfo.currentParticipants}/${classInfo.maxParticipants} (마감)"
                }
            } else {
                "${classInfo.currentParticipants}/${classInfo.maxParticipants} (잔여 ${remainingCount})"
            }
            Text(participantsText, color = participantsColor, fontSize = 10.sp)
            Spacer(Modifier.weight(1f))
            CoachViewButton(enabled = !isClosed && enabled, onClick = onCoachClick) // [수정] enabled도 확인
        }
    }
}

@Composable
private fun CoachViewButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val color = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
    Box(
        modifier = Modifier
            .size(width = 95.dp, height = 32.dp)
            .border(width = 1.dp, color = color, shape = RoundedCornerShape(5.dp))
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("코치 보기", color = color, fontSize = 12.sp)
    }
}

/* -------------------------- Preview -------------------------- */

@Preview(showBackground = true)
@Composable
private fun ClassCardPreview() {
    val sampleClass = SampleClassInfo(
        id = "1",
        coachId = "c1",
        date = LocalDate.of(2025, 11, 28),
        time = "14:00",
        type = "일반 클래스",
        imageUrl = null,
        className = "직장인을 위한 코어 강화",
        coachName = "김리본 코치",
        description = "점심시간을 활용한 30분 집중 코어 운동 클래스입니다.",
        currentParticipants = 7,
        maxParticipants = 10
    )
    LivonTheme {
        Box(Modifier.padding(16.dp)) {
            ClassCard(
                classInfo = sampleClass,
                onCardClick = {},
                onCoachClick = {},
                enabled = true
            )
        }
    }
}

@Preview(showBackground = true, name = "마감된 클래스 카드")
@Composable
private fun ClassCardClosedPreview() {
    val sampleClass = SampleClassInfo(
        id = "2",
        coachId = "c2",
        date = LocalDate.of(2025, 11, 29),
        time = "19:00",
        type = "기업 클래스",
        imageUrl = null,
        className = "퇴근 후 스트레칭",
        coachName = "박생존 코치",
        description = "하루의 피로를 풀어주는 힐링 스트레칭 시간입니다.",
        currentParticipants = 10,
        maxParticipants = 10
    )
    LivonTheme {
        Box(Modifier.padding(16.dp)) {
            ClassCard(
                classInfo = sampleClass,
                onCardClick = {},
                onCoachClick = {},
                enabled = true
            )
        }
    }
}
