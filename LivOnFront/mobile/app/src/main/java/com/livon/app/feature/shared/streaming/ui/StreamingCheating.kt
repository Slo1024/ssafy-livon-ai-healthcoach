package com.livon.app.feature.shared.streaming.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livon.app.feature.shared.streaming.util.DateFormatter
import com.livon.app.feature.shared.streaming.vm.StreamingChatViewModel
import com.livon.app.ui.component.streaming.CheatingBar
import com.livon.app.ui.component.streaming.StreamingCheatingHeader
import com.livon.app.ui.component.streaming.StreamingCheatingProfile
import com.livon.app.ui.theme.LivonTheme

import com.livon.app.BuildConfig

@Composable
fun StreamingCheating(
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StreamingChatViewModel = viewModel(),
    chatRoomId: Int = 43
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChatMessages(chatRoomId = chatRoomId)
    }

    LivonTheme {
        Scaffold(
            topBar = {
                StreamingCheatingHeader(onBackClick = onBackClick)
            },
            bottomBar = {
                CheatingBar(
                    modifier = Modifier.fillMaxWidth(),
                    onSend = { message ->
                        viewModel.sendMessage(message, BuildConfig.WEBSOCKET_TOKEN, chatRoomId = chatRoomId)
                    }
                )
            }
        ) { paddingValues ->
            val error = uiState.error
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        items(uiState.messages) { message ->
                            StreamingCheatingProfile(
                                userName = message.userId,
                                message = message.content,
                                time = DateFormatter.formatToTime(message.sentAt)
                            )
                        }
                    }
                }
            }
        }
    }
}

