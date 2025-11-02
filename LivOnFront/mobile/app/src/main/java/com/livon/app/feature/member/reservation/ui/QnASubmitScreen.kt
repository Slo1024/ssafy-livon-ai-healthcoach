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
                        .height(56.dp), // 고정 높이로 Scaffold가 innerPadding을 계산하게 함
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
        // body가 bottomBar 높이를 고려하도록 Column + weight 사용
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // 하단 바를 위해 공간을 남김
                    .fillMaxWidth()
                    .padding(innerPadding) // Scaffold가 제공하는 패딩 적용
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 기존 LazyColumn 내용(아이템들)을 여기 그대로 유지하세요.
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
    Column(modifier = Modifier.padding(top = 25.dp)) {
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(160.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Spacer(Modifier.height(16.dp))
                Text("예약이 완료 되었습니다.", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
                Text(
                    text = "내 건강 정보를 바꾸고 싶으신가요?",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = LiveRed,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable(onClick = onChangeHealthInfo)
                )
                HorizontalDivider(color = Gray, thickness = 1.dp)
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
