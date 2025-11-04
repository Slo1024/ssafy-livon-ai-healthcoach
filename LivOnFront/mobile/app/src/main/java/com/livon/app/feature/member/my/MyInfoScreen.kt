// com/livon/app/feature/member/my/MyInfoScreen.kt
package com.livon.app.feature.member.my

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.tooling.preview.Preview
import com.livon.app.R
import com.livon.app.feature.shared.auth.ui.CommonScreenC
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Spacing

/** 회원가입 로직에서 모아둔 값을 화면에 전달하기 위한 UI 상태 */
@Immutable
data class MyInfoUiState(
    // 프로필 영역
    val nickname: String,
    val gender: String?,                 // "남성", "여성" 등 (없으면 null)
    val birthday: String?,               // 예) "2000년 01월 23일" (포맷은 외부에서 처리)
    val profileImageUri: Uri?,           // ProfileSelectScreen에서 고른 이미지 (없으면 null)

    // 건강 정보/생활 습관 (없을 수도 있으니 null 허용)
    val heightCm: String?,               // "170cm" 형태로 넘겨도 되고, 숫자만 넘겨도 됨
    val weightKg: String?,               // "70kg"
    val condition: String?,              // 기저질환
    val sleepQuality: String?,           // 수면 상태
    val medication: String?,             // 복약 여부/종류
    val painArea: String?,               // 통증/불편 부위
    val stress: String?,                 // 스트레스
    val smoking: String?,                // 흡연 여부
    val alcohol: String?,                // 음주 빈도
    val sleepHours: String?,             // "6시간"
    val activityLevel: String?,          // (옵션) 활동 수준
    val caffeine: String?                // (옵션) 카페인 섭취
)

/** 시안: 나의 건강 정보 */
@Composable
fun MyInfoScreen(
    state: MyInfoUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEditClick: () -> Unit = {}         // 우상단 '수정'
) {
    CommonScreenC(
        modifier = modifier,
        topBar = { m ->
            Box(m.fillMaxWidth()) {
                TopBar(title = "나의 건강 정보", onBack = onBack)
                Text(
                    text = "수정",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = Spacing.Horizontal)
                        .clickable { onEditClick() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )
            }
        },
        fullBleedContent = {
            // Grid in full-bleed area (we keep internal horizontal spacing via contentPadding)
            data class InfoField(val label: String, val value: String)

            val tiles = buildList<InfoField> {
                state.heightCm?.takeIf { it.isNotBlank() }?.let { add(InfoField("키", it.withUnitIfMissing("cm"))) }
                state.weightKg?.takeIf { it.isNotBlank() }?.let { add(InfoField("몸무게", it.withUnitIfMissing("kg"))) }
                state.condition?.takeIf { it.isNotBlank() }?.let { add(InfoField("질환", it)) }
                state.sleepQuality?.takeIf { it.isNotBlank() }?.let { add(InfoField("수면 상태", it)) }
                state.medication?.takeIf { it.isNotBlank() }?.let { add(InfoField("복약 여부", it)) }
                state.painArea?.takeIf { it.isNotBlank() }?.let { add(InfoField("통증 부위", it)) }
                state.stress?.takeIf { it.isNotBlank() }?.let { add(InfoField("스트레스", it)) }
                state.smoking?.takeIf { it.isNotBlank() }?.let { add(InfoField("흡연 여부", it)) }
                state.alcohol?.takeIf { it.isNotBlank() }?.let { add(InfoField("음주", it)) }
                state.sleepHours?.takeIf { it.isNotBlank() }?.let { add(InfoField("수면 시간", it)) }
                state.activityLevel?.takeIf { it.isNotBlank() }?.let { add(InfoField("활동 수준", it)) }
                state.caffeine?.takeIf { it.isNotBlank() }?.let { add(InfoField("카페인", it)) }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = Spacing.Horizontal, vertical = 24.dp)
            ) {
                items(tiles) { field ->
                    InfoTile(label = field.label, value = field.value)
                }
            }
        }
    ) {
        Spacer(Modifier.height(20.dp))

        // ── 프로필 영역 ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = state.profileImageUri?.let { rememberAsyncImagePainter(it) }
                ?: painterResource(id = R.drawable.ic_noprofile)

            Image(
                painter = painter,
                contentDescription = "프로필",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentScale = ContentScale.Crop,
                colorFilter = null as ColorFilter?
            )

            Spacer(Modifier.width(20.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.nickname,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    state.gender?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                state.birthday?.let { bday ->
                    Spacer(Modifier.height(15.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "생년월일",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = bday,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }

}

/** 카드 하나(라운드+살짝 그림자) */
@Composable
private fun InfoTile(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // reduce space between label and value to 6dp
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = value,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** "170" -> "170cm" 처럼 단위 없으면 붙여주기 (간단 보정) */
private fun String.withUnitIfMissing(unit: String): String {
    val trimmed = trim()
    return if (trimmed.endsWith(unit)) trimmed else "$trimmed$unit"
}

/* ───────── Preview ───────── */
@Preview(showBackground = true, showSystemUi = true, name = "MyInfoScreen")
@Composable
private fun PreviewMyInfoScreen() = PreviewSurface {
    MyInfoScreen(
        state = MyInfoUiState(
            nickname = "김싸피",
            gender = "남성",
            birthday = "20XX년 XX월 XX일",
            profileImageUri = null, // 등록 안 했으면 null → ic_noprofile 사용
            heightCm = "170",
            weightKg = "70",
            condition = "고혈압",
            sleepQuality = "자주 깨거나 뒤척임",
            medication = "혈압약",
            painArea = "허리",
            stress = "거의 없음",
            smoking = "비흡연",
            alcohol = "하지 않음",
            sleepHours = "6시간",
            activityLevel = null,
            caffeine = null
        ),
        onBack = {},
        onEditClick = {}
    )
}
