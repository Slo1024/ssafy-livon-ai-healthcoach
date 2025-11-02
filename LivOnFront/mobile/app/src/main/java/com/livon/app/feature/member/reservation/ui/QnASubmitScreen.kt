package com.livon.app.feature.member.reservation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.Gray
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.LiveRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.livon.app.ui.component.navbar.HomeNavBar

@Composable
fun QnASubmitScreen(
    coachName: String,
    selectedDate: LocalDate,
    onBack: () -> Unit,
    onConfirmReservation: (List<String>) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToMyHealthInfo: () -> Unit,
) {
    var questions by remember { mutableStateOf(listOf("", "")) }
    var showDialog by remember { mutableStateOf(false) }

    val formattedDate = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))
    }

    Scaffold(
        topBar = { TopBar(title = "Q&A", onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                PrimaryButtonBottom(
                    text = "예약 확정하기",
                    enabled = true,
                    onClick = { showDialog = true }
                )

                Spacer(Modifier.height(8.dp))

                HomeNavBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    currentRoute = null,
                    onNavigate = { route ->
                        when (route) {
                            "home" -> onNavigateHome()
                            "mypage" -> onNavigateToMyHealthInfo()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // body - 상단고정(TopBar) / 하단고정(bottomBar) 사이에서 스크롤 가능 영역
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // 하단 바를 위해 공간을 남김
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 헤더: 코치명, 날짜
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$coachName 코치",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = Color.Gray),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // 질문 입력 리스트
                itemsIndexed(questions) { index, question ->
                    QnaInputItem(
                        question = question,
                        onValueChange = { new ->
                            questions = questions.toMutableList().also { it[index] = new }
                        },
                        onDelete = {
                            questions = questions.toMutableList().also { it.removeAt(index) }
                        }
                    )
                }

                // 질문 추가 버튼
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "질문 추가",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    questions = questions + ""
                                }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // 여유 공간 (하단 버튼 가림 방지)
                item {
                    Spacer(Modifier.height(64.dp))
                }
            }
        }
    }

    if (showDialog) {
        ReservationCompleteDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                onConfirmReservation(questions.filter { it.isNotBlank() })
                onNavigateHome()
            },
            onChangeHealthInfo = {
                showDialog = false
                onNavigateToMyHealthInfo()
            }
        )
    }
}

@Composable
private fun QnaInputItem(
    question: String,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    // 상단 패딩 축소(시각적으로 여유 줄임)
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Q", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (question.isNotEmpty()) {
                Text(
                    text = "질문 삭제",
                    color = LiveRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable(onClick = onDelete).padding(4.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = question,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFE9E9E9))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.TopStart) {
                    if (question.isEmpty()) {
                        Text(
                            "내용을 입력해주세요",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                            color = Color(0xFF7B7B7B)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun ReservationCompleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onChangeHealthInfo: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        // 플랫폼 기본 너비 사용 (다이얼로그 잘림 방지)
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "예약이 완료 되었습니다.",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "내 건강 정보를 바꾸고 싶으신가요?",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LiveRed,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .clickable(onClick = onChangeHealthInfo)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
                Divider(color = Gray, thickness = 1.dp)
                Text(
                    text = "확인",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QnASubmitScreenPreview() {
    LivonTheme {
        QnASubmitScreen(
            coachName = "김싸피",
            selectedDate = LocalDate.of(2025, 9, 18),
            onBack = {},
            onConfirmReservation = {},
            onNavigateHome = {},
            onNavigateToMyHealthInfo = {}
        )
    }
}

@Preview(showBackground = true, name = "Q&A 입력 항목")
@Composable
private fun QnaInputItemPreview() {
    var text by remember { mutableStateOf("운동 후 식단 관리에 대해 질문하고 싶습니다.") }
    LivonTheme {
        Column(Modifier.padding(16.dp)) {
            QnaInputItem(
                question = text,
                onValueChange = { text = it },
                onDelete = {}
            )
            QnaInputItem(
                question = "",
                onValueChange = {},
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "예약 완료 다이얼로그")
@Composable
private fun ReservationCompleteDialogPreview() {
    LivonTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ReservationCompleteDialog(
                onDismiss = {},
                onConfirm = {},
                onChangeHealthInfo = {}
            )
        }
    }
}
