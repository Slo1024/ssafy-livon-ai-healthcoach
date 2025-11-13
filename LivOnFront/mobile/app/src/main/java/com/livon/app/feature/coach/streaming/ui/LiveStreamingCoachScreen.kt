package com.livon.app.feature.coach.streaming.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.livon.app.feature.shared.streaming.RoomUiState
import com.livon.app.feature.shared.streaming.TrackInfo
import com.livon.app.feature.shared.streaming.ui.StreamingCheating
import com.livon.app.feature.shared.streaming.ui.StreamingParticipant
import com.livon.app.ui.component.streaming.CoachStreamingHeader
import com.livon.app.ui.component.streaming.MemberStreamingCamera
import com.livon.app.ui.component.streaming.StreamingCamera
import com.livon.app.ui.component.streaming.StreamingNav
import com.livon.app.ui.theme.LivonTheme
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.room.track.Track
import livekit.org.webrtc.EglBase


@Composable
fun LiveStreamingCoachScreen(
    uiState: RoomUiState,
    participantTracks: List<TrackInfo>,
    eglBaseContext: EglBase.Context,
    room: Room,
    onLeaveRoom: () -> Unit,
    onConnect: () -> Unit,
    onToggleCamera: () -> Unit = {},
    onToggleMic: () -> Unit = {},
    onShareScreen: () -> Unit = {},
    consultationId: Long,
    jwtToken: String,
    onToggleSpeaker: () -> Unit = {},
    isSpeakerMuted: Boolean = false,
) {
    LaunchedEffect(key1 = true) {
        onConnect()
    }

    // 세션 참가 시 웹소켓 연결, 구독, POST 요청 수행
    val chatViewModel: com.livon.app.feature.shared.streaming.vm.StreamingChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.livon.app.feature.shared.streaming.vm.StreamingChatViewModel(
                    consultationId = consultationId,
                    jwtToken = jwtToken
                ) as T
            }
        }
    )

    var currentScreen by remember { mutableStateOf("streaming") } // "streaming", "participant", "chatting"
    var showHeader by remember { mutableStateOf(false) } // default: header hidden

    LivonTheme {
        when (currentScreen) {
            "participant" -> {
                StreamingParticipant(
                    onBackClick = { currentScreen = "streaming" },
                    onSearch = {}
                )
            }

            "chatting" -> {
                StreamingCheating(
                    onBackClick = { currentScreen = "streaming" },
                    onSearch = { /* 검색 기능은 추후 구현 */ },
                    viewModel = chatViewModel,
                    chatRoomId = consultationId.toInt()
                )
            }

            else -> {
                Scaffold(
                    topBar = {
                        if (showHeader) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .zIndex(1f)
                            ) {
                                CoachStreamingHeader(
                                    roomName = uiState.roomName,
                                    onLeaveRoom = onLeaveRoom,
                                    onPersonClick = { currentScreen = "participant" },
                                    onChatClick = { currentScreen = "chatting" },
                                    onSoundClick = onToggleSpeaker,
                                    isSoundMuted = isSpeakerMuted
                                )
                            }
                        }
                    },
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .zIndex(1f)
                        ) {
                            var micEnabled by remember { mutableStateOf(true) }
                            var cameraEnabled by remember { mutableStateOf(true) }
                            StreamingNav(
                                isMicEnabled = micEnabled,
                                isCameraEnabled = cameraEnabled,
                                onToggleMic = {
                                    onToggleMic()
                                    micEnabled = !micEnabled
                                },
                                onToggleCamera = {
                                    onToggleCamera()
                                    cameraEnabled = !cameraEnabled
                                },
                                onShare = { onShareScreen() },
                                onMore = { showHeader = !showHeader },
                                onExit = onLeaveRoom
                            )
                        }
                    }
                ) { paddingValues ->

                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.isError) {
                        Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                            Text("연결 오류: ${uiState.errorMessage}")
                        }
                    } else {
                        val coachTrackInfo = participantTracks.firstOrNull { it.isLocal }
                        val remoteTracks = participantTracks.filter { !it.isLocal }

                        Log.d(
                            "LiveStreamingCoachScreen",
                            "Participant tracks count: ${participantTracks.size}"
                        )
                        Log.d("LiveStreamingCoachScreen", "Coach track info: $coachTrackInfo")
                        Log.d(
                            "LiveStreamingCoachScreen",
                            "Remote tracks count: ${remoteTracks.size}"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            when (remoteTracks.size) {
                                0 -> {
                                    coachTrackInfo?.let { me ->
                                        StreamingCamera(
                                            track = me.track as? VideoTrack,
                                            userName = me.participantIdentity,
                                            isCameraEnabled = me.isCameraEnabled,
                                            eglBaseContext = eglBaseContext,
                                            room = room,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } ?: Text("비디오 스트림을 준비 중입니다.")
                                }

                                1 -> {
                                    val remote = remoteTracks.first()
                                    StreamingCamera(
                                        track = remote.track as? VideoTrack,
                                        userName = remote.participantIdentity,
                                        isCameraEnabled = remote.isCameraEnabled,
                                        eglBaseContext = eglBaseContext,
                                        room = room,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    coachTrackInfo?.let { me ->
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(12.dp)
                                                .size(140.dp)
                                        ) {
                                            StreamingCamera(
                                                track = me.track as? VideoTrack,
                                                userName = me.participantIdentity,
                                                isCameraEnabled = me.isCameraEnabled,
                                                eglBaseContext = eglBaseContext,
                                                room = room,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    val pageSize = 4
                                    val pages = (remoteTracks.size + pageSize - 1) / pageSize

                                    LazyRow(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(pages) { pageIndex ->
                                            val start = pageIndex * pageSize
                                            val end = minOf(start + pageSize, remoteTracks.size)

                                            Box(
                                                modifier = Modifier
                                                    .fillParentMaxWidth()
                                                    .fillParentMaxHeight()
                                                    .padding(8.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                    ) {
                                                        for (i in 0 until 2) {
                                                            val idx = start + i
                                                            if (idx < end) {
                                                                val t = remoteTracks[idx]
                                                                Box(modifier = Modifier.weight(1f)) {
                                                                    StreamingCamera(
                                                                        track = t.track as? VideoTrack,
                                                                        userName = t.participantIdentity,
                                                                        isCameraEnabled = t.isCameraEnabled,
                                                                        eglBaseContext = eglBaseContext,
                                                                        room = room,
                                                                        modifier = Modifier.fillMaxSize()
                                                                    )
                                                                }
                                                            } else {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                    ) {
                                                        for (i in 2 until 4) {
                                                            val idx = start + i
                                                            if (idx < end) {
                                                                val t = remoteTracks[idx]
                                                                Box(modifier = Modifier.weight(1f)) {
                                                                    StreamingCamera(
                                                                        track = t.track as? VideoTrack,
                                                                        userName = t.participantIdentity,
                                                                        isCameraEnabled = t.isCameraEnabled,
                                                                        eglBaseContext = eglBaseContext,
                                                                        room = room,
                                                                        modifier = Modifier.fillMaxSize()
                                                                    )
                                                                }
                                                            } else {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                }

                                                coachTrackInfo?.let { me ->
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .padding(12.dp)
                                                            .size(140.dp)
                                                    ) {
                                                        MemberStreamingCamera(
                                                            track = me.track as? VideoTrack,
                                                            userName = me.participantIdentity,
                                                            isCameraEnabled = me.isCameraEnabled,
                                                            eglBaseContext = eglBaseContext,
                                                            room = room,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}