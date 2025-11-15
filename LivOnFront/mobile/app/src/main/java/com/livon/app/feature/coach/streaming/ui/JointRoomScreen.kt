package com.livon.app.feature.coach.streaming.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.R
import com.livon.app.feature.shared.streaming.QuickRoomUiState
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun JoinRoomScreen(
    initialParticipantName: String,
    initialRoomName: String,
    onJoinClicked: (roomName: String, participantName: String) -> Unit,
    modifier: Modifier = Modifier,
    quickRoomState: QuickRoomUiState = QuickRoomUiState(),
    onCreateRoomClicked: ((participantName: String) -> Unit)? = null
) {
    var participantName by remember { mutableStateOf(initialParticipantName) }
    var roomName by remember { mutableStateOf(initialRoomName) }
    var isJoinButtonEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.join_room),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 48.dp, bottom = 20.dp)
        )

        Text(
            text = stringResource(id = R.string.participant),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp)
        )
        OutlinedTextField(
            value = participantName,
            onValueChange = { participantName = it },
            label = { Text("Participant Name") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        )

        Text(
            text = stringResource(id = R.string.room),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        )
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("Room Name") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        )

        Button(
            onClick = {
                isJoinButtonEnabled = false
                onJoinClicked(roomName, participantName)
            },
            enabled = isJoinButtonEnabled,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(stringResource(id = R.string.joinBtn), fontWeight = FontWeight.Bold)
        }

        if (onCreateRoomClicked != null) {
            Divider(modifier = Modifier
                .padding(vertical = 24.dp)
                .fillMaxWidth())

            Text(
                text = "빠르게 방 만들기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (!quickRoomState.errorMessage.isNullOrBlank()) {
                Text(
                    text = quickRoomState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Button(
                onClick = { onCreateRoomClicked(participantName) },
                enabled = !quickRoomState.isCreating,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                if (quickRoomState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                    Text("생성 중...")
                } else {
                    Text("새 상담 방 생성", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
