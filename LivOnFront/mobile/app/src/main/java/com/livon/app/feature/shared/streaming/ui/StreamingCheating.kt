package com.livon.app.feature.shared.streaming.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livon.app.feature.shared.streaming.util.DateFormatter
import com.livon.app.feature.shared.streaming.vm.StreamingChatViewModel
import com.livon.app.ui.component.streaming.CheatingBar
import com.livon.app.ui.component.streaming.StreamingCheatingHeader
import com.livon.app.ui.component.streaming.StreamingCheatingProfile
import com.livon.app.ui.theme.LivonTheme

import com.livon.app.BuildConfig
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun StreamingCheating(
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StreamingChatViewModel = viewModel(),
    chatRoomId: Int = 43
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    data class ScrollRestore(
        val messageId: String?,
        val index: Int,
        val offset: Int,
        val previousSize: Int
    )
    var pendingScrollRestore by remember { mutableStateOf<ScrollRestore?>(null) }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadChatMessages(chatRoomId = chatRoomId, isInitialLoad = true)
    }

    LaunchedEffect(listState, viewModel) {
        var isFirstEmission = true
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                if (isFirstEmission) {
                    isFirstEmission = false
                    return@collect
                }
                val state = viewModel.uiState.value
                if (
                    !state.isLoadingPast &&
                    !state.isLoading &&
                    !state.isLastPage &&
                    state.messages.isNotEmpty() &&
                    index == 0 &&
                    offset == 0
                ) {
                    val anchorId = state.messages.getOrNull(index)?.id
                    pendingScrollRestore = ScrollRestore(anchorId, index, offset, state.messages.size)
                    viewModel.loadChatMessages(chatRoomId = chatRoomId, isInitialLoad = false)
                }
            }
    }

    val hasScrolledToBottom = remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoadingPast, uiState.showPastLoadingIndicator, uiState.messages.size) {
        val pending = pendingScrollRestore
        if (pending != null && !uiState.isLoadingPast && !uiState.showPastLoadingIndicator) {
            val anchorIndex = pending.messageId?.let { anchorId ->
                uiState.messages.indexOfFirst { it.id == anchorId }
            }?.takeIf { it >= 0 }

            val fallbackAdded = uiState.messages.size - pending.previousSize
            val targetIndex = when {
                anchorIndex != null -> (anchorIndex - 1).coerceAtLeast(0)
                fallbackAdded > 0 -> (pending.index + fallbackAdded - 1).coerceAtLeast(0).coerceAtMost(uiState.messages.lastIndex)
                else -> pending.index.coerceAtMost(uiState.messages.lastIndex)
            }

            if (targetIndex >= 0) {
                listState.scrollToItem(targetIndex, pending.offset)
            }
            pendingScrollRestore = null
        }
    }

    LivonTheme {
        LaunchedEffect(uiState.messages.size) {
            if (
                uiState.messages.isNotEmpty() &&
                !hasScrolledToBottom.value &&
                !uiState.isLoadingPast &&
                !uiState.showPastLoadingIndicator
            ) {
                listState.scrollToItem(uiState.messages.lastIndex)
                hasScrolledToBottom.value = true
            }
        }

        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .zIndex(1f)
                ) {
                    StreamingCheatingHeader(onBackClick = onBackClick)
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .zIndex(1f)
                ) {
                    CheatingBar(
                        modifier = Modifier.fillMaxWidth(),
                        onSend = { message ->
                            viewModel.sendMessage(message, BuildConfig.WEBSOCKET_TOKEN, chatRoomId = chatRoomId)
                        }
                    )
                }
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (uiState.showPastLoadingIndicator) {
                                item(key = "past_loading_indicator") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            items(uiState.messages, key = { it.id }) { message ->
                                StreamingCheatingProfile(
                                    userName = message.userId,
                                    message = message.content,
                                    time = DateFormatter.formatToTime(message.sentAt),
                                    role = message.role
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

