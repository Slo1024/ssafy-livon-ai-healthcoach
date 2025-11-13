package com.livon.app.feature.member.streaming.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamingMemberScreen(
    sessionId: String? = null,
    onEnter: () -> Unit = {},
    onLeave: () -> Unit = {}
) {
    // 세션 ID가 없으면 모달을 띄우기 위한 상태
    val showNoSessionDialog = remember { mutableStateOf(sessionId.isNullOrBlank()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("세션") })
        }
    ) { padding ->
        // 세션이 없을 경우 AlertDialog로 안내
        if (showNoSessionDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    // 다이얼로그 밖을 누르면 떠나도록 처리
                    showNoSessionDialog.value = false
                    onLeave()
                },
                title = { Text(text = "세션 준비 중") },
                text = { Text(text = "아직 코치가 상담 세션을 생성하지 않았습니다.") },
                confirmButton = {
                    TextButton(onClick = {
                        showNoSessionDialog.value = false
                        // 확인 시 이전 화면으로 복귀
                        onLeave()
                    }) {
                        Text("확인")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (sessionId.isNullOrBlank()) {
                    // 세션이 없다는 메시지 (대체 UI)
                    Text(text = "세션이 아직 생성되지 않았습니다. 잠시 후 다시 시도해주세요.")
                    Spacer(modifier = Modifier.height(16.dp))
                    // 디버그: 입장하기 버튼을 눌러도 모달이 뜨도록 유지
                    Button(onClick = { showNoSessionDialog.value = true }) {
                        Text(text = "입장하기")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onLeave) {
                        Text(text = "뒤로가기")
                    }
                } else {
                    // 기존 라이브 세션 연결 UI
                    Text(text = "라이브 세션에 연결 중...\nsessionId=${sessionId}")
                    Spacer(modifier = Modifier.height(16.dp))
                    // 세션이 존재하면 입장 콜백을 호출하도록 노출
                    Button(onClick = onEnter) {
                        Text(text = "입장하기")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onLeave) {
                        Text(text = "나가기")
                    }
                }
            }
        }
    }
}
