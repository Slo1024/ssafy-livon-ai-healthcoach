package com.livon.app.feature.coach.streaming.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.ui.theme.LivonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureUrlsScreen(
    onSaveClicked: (serverUrl: String, livekitUrl: String) -> Unit = { _, _ -> }
) {
    var serverUrl by remember { mutableStateOf("http://10.43.255.51:6080/") } // 초기값 설정
    var livekitUrl by remember { mutableStateOf("ws://10.43.255.51:7880") } // 초기값 설정

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // 상단 정렬
    ) {
        // Application Server URL 입력 필드
        Text(
            text = stringResource(id = R.string.application_server_url),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start) // 왼쪽 정렬
                .padding(top = 20.dp, start = 16.dp)
        )
        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            label = { Text("Application Server URL") }, // 힌트 텍스트
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true
        )

        // LiveKit URL 입력 필드
        Text(
            text = stringResource(id = R.string.livekit_url),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start) // 왼쪽 정렬
                .padding(top = 16.dp, start = 16.dp)
        )
        OutlinedTextField(
            value = livekitUrl,
            onValueChange = { livekitUrl = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            label = { Text("LiveKit URL") }, // 힌트 텍스트
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true
        )

        // 저장 버튼
        Button(
            onClick = { onSaveClicked(serverUrl, livekitUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .height(50.dp) // 버튼 높이 조절
        ) {
            Text(
                text = stringResource(id = R.string.save),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}