package com.livon.app.feature.shared.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.preview.PreviewSurface
import com.livon.app.ui.theme.Gray
import com.livon.app.ui.theme.Gray2
import com.livon.app.ui.theme.Main

@Composable
fun TermOfUseScreen(
    onClickNext: () -> Unit = {},
    onClickBack: () -> Unit = {}
) {
    var agreeAll by remember { mutableStateOf(false) }
    var agreeService by remember { mutableStateOf(false) }  // (필수) 서비스 이용약관 동의
    var agreePrivacy by remember { mutableStateOf(false) }  // (필수) 개인정보 처리방침 동의
    var agreeMarketing by remember { mutableStateOf(false) } // (선택) 마케팅 수신 동의

    // 전체 동의 토글 -> 모든 항목 동기화
    fun toggleAll(value: Boolean) {
        agreeAll = value
        agreeService = value
        agreePrivacy = value
        agreeMarketing = value
    }

    // 개별 체크 변화 시 전체동의 상태 재계산
    fun recomputeAll() {
        agreeAll = agreeService && agreePrivacy && agreeMarketing
    }

    val enabledNext = agreeService && agreePrivacy
    CommonSignUpScreenA(
        topBar = { TopBar(title = "", onBack = onClickBack) },
        bottomBar = {
            PrimaryButtonBottom(
                text = "완료",
                onClick = onClickNext,
                enabled = enabledNext
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 타이틀 : LIVON 강조
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Main, fontWeight = FontWeight.SemiBold)) {
                        append("LIVON ")
                    }
                    append("서비스 이용약관에\n동의해주세요")
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    color = Gray2
                )
            )

            Spacer(Modifier.height(300.dp))

            // 약관 카드 (배경 살짝 구분)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 2.dp)
                    .padding(top = 4.dp)
            ) {
                // (필수) 2개 + (선택) 1개
                TermsRow(
                    checked = agreeService,
                    onCheckedChange = { agreeService = it; recomputeAll() },
                    title = "서비스 이용약관 동의",
                    required = true
                )
                TermsRow(
                    checked = agreePrivacy,
                    onCheckedChange = { agreePrivacy = it; recomputeAll() },
                    title = "개인정보 처리방침 동의",
                    required = true
                )
                TermsRow(
                    checked = agreeMarketing,
                    onCheckedChange = { agreeMarketing = it; recomputeAll() },
                    title = "마케팅 수신 동의",
                    required = false
                )
            }

            Spacer(Modifier.height(16.dp))

            // 전체 약관 동의
            TermsAllRow(
                checked = agreeAll,
                onCheckedChange = { toggleAll(it) }
            )

            // 도움문구/회색 영역(피그마 하단 설명 영역 느낌)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "필수 약관에 동의해야 계속 진행할 수 있어요.",
                color = Gray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(2.dp)) // Primary 버튼과 체크 영역 사이를 약 60dp로 확보
        }
    }
}

@Composable
private fun TermsRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    required: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // drawable 기반 체크박스(프리뷰와 동일하게 이미지로 표현)
        Image(
            painter = painterResource(id = if (checked) R.drawable.checkmark else R.drawable.graycheckbox),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .clickable { onCheckedChange(!checked) }
        )
        Spacer(Modifier.width(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Gray2,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (required) "필수" else "선택",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (required) MaterialTheme.colorScheme.error else Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun TermsAllRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = if (checked) R.drawable.allchecked else R.drawable.grayallcheck),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .clickable { onCheckedChange(!checked) }
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "전체 약관 동의",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Gray2,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        )
    }
}



@Composable
private fun TermOfUseScreenPreviewInternal(
    agreeService: Boolean,
    agreePrivacy: Boolean,
    agreeMarketing: Boolean
) {
    val enabledNext = agreeService && agreePrivacy
    val agreeAll = agreeService && agreePrivacy && agreeMarketing

    CommonSignUpScreenA(
        topBar = { TopBar(title = "이용약관", onBack = {}) },
        bottomBar = {
            PrimaryButtonBottom(
                text ="완료",
                onClick = {},
                enabled = enabledNext
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Main, fontWeight = FontWeight.SemiBold)) {
                        append("LIVON ")
                    }
                    append("서비스 이용약관에\n동의해주세요")
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    color = Gray2
                )
            )

            Spacer(Modifier.height(320.dp))

            // 각 약관 항목을 이모지 대신 drawable로 표현
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        id = if (agreeService) R.drawable.checkmark else R.drawable.graycheckbox
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("[필수] 서비스 이용약관 동의", color = Gray2, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        id = if (agreePrivacy) R.drawable.checkmark else R.drawable.graycheckbox
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("[필수] 개인정보 처리방침 동의", color = Gray2, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        id = if (agreeMarketing) R.drawable.checkmark else R.drawable.graycheckbox
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("[선택] 마케팅 수신 동의", color = Gray2, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            // 전체 약관 동의 표시: 체크 상태에 따라 다른 drawable 사용
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        id = if (agreeAll) R.drawable.allchecked else R.drawable.grayallcheck
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("전체 약관 동의", color = Gray2, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(8.dp))
            Text("서비스 이용을 위해 위 약관들을 모두 동의합니다.", color = Gray, fontSize = 13.sp)
        }
    }
}


