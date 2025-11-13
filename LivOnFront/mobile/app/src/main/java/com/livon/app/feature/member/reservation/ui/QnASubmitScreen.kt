package com.livon.app.feature.member.reservation.ui

import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.component.button.PrimaryButtonBottom
import com.livon.app.ui.component.navbar.HomeNavBar
import com.livon.app.ui.component.overlay.TopBar
import com.livon.app.ui.theme.Gray
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.LiveRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun QnASubmitScreen(
    coachName: String,
    selectedDate: LocalDate,
    onBack: () -> Unit,
    onConfirmReservation: (List<String>) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToMyHealthInfo: () -> Unit,
    navController: NavHostController? = null, // optional navController for HomeNavBar navigation
    externalError: String? = null // external error message (e.g., 409) to show in snackbar
) {
    var questions by rememberSaveable { mutableStateOf(listOf("", "")) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    // If we returned from health flow, show the dialog again
    // Observe savedStateHandle key reliably using StateFlow -> collectAsState
    val backStackEntry = navController?.currentBackStackEntry
    val savedStateHandle = backStackEntry?.savedStateHandle
    val healthUpdatedFlow = remember(savedStateHandle) { savedStateHandle?.getStateFlow("health_updated", false) }
    val healthUpdated by (healthUpdatedFlow?.collectAsState(initial = false) ?: remember { mutableStateOf(false) })

    LaunchedEffect(healthUpdated) {
        if (healthUpdated) {
            showDialog = true
        }
    }

    val formattedDate = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))
    }

    // local snackbar host for showing errors like 409
    val snackbarHostState = remember { SnackbarHostState() }

    // 외부에서 전달된 error를 스낵바로 보여줌
    LaunchedEffect(externalError) {
        externalError?.let { msg ->
            if (msg.isNotBlank()) snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            // 상단 안전영역(상태바)
            Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                TopBar(title = "Q&A", onBack = onBack)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // 하단 안전영역(제스처바/IME)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues())
            ) {
                // 버튼-내비바 ‘붙이기’: 내부 navPadding 끄고, bottomMargin=0
                PrimaryButtonBottom(
                    text = "예약 확정하기",
                    enabled = true,
                    onClick = { showDialog = true },
                    bottomMargin = 0.dp,
                    applyNavPadding = false
                )
                HomeNavBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    currentRoute = null,
                    navController = navController,
                    onNavigate = { route ->
                        // Fallback behavior when navController not provided
                        when (route) {
                            "home" -> onNavigateHome()
                            "mypage" -> onNavigateToMyHealthInfo()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 중앙 정렬 헤더
            item {
                Spacer(Modifier.height(8.dp))

                val headerText = buildString {
                    append(formattedDate)
                    append(" ")
                    append(coachName)
                    append(" 코치와의 상담 전\nQ&A를 등록해보세요.")
                }

                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(12.dp))
            }

            // 질문 입력 리스트
            itemsIndexed(questions, key = { idx, _ -> idx }) { index, question ->
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

            // “추가 +” ( + 는 아이콘 )
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { questions = questions + "" }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "추가",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "추가",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // 다이얼로그는 LazyColumn 밖
    if (showDialog) {
        ReservationCompleteDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                onConfirmReservation(questions.filter { it.isNotBlank() })
                // NOTE: navigation (to reservations/home) is handled by caller via onConfirmReservation in NavGraph
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
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Q",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (question.isNotEmpty()) {
                Text(
                    text = "질문 삭제",
                    color = LiveRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .clickable(onClick = onDelete)
                        .padding(4.dp)
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
    try { android.util.Log.d("ReservationDialog", "Composing ReservationCompleteDialog") } catch (_: Throwable) {}
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.60f))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            )
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .heightIn(min = 220.dp, max = 420.dp), // 최소/최대 높이 지정
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Added a small close control at the top-right so user can dismiss the dialog
                Box(modifier = Modifier.fillMaxWidth()) {
                    // spacer to keep title centered
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "X",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable(onClick = onDismiss)
                            .padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
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
                }

                HorizontalDivider(
                    color = Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

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
