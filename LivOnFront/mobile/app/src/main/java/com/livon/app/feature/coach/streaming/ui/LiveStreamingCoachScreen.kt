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
    var focusedTrackSid by remember { mutableStateOf<String?>(null) } // double-tap focus

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
                            focusedTrackSid?.let { sid ->
                                val focused = participantTracks.firstOrNull { it.track?.sid == sid }
                                if (focused != null) {
                                    StreamingCamera(
                                        track = focused.track as? VideoTrack,
                                        userName = focused.participantIdentity,
                                        isCameraEnabled = focused.isCameraEnabled,
                                        isScreenShare = focused.isScreenShare,
                                        eglBaseContext = eglBaseContext,
                                        room = room,
                                        modifier = Modifier.fillMaxSize(),
                                        onDoubleTap = { focusedTrackSid = null }
                                    )
                                    return@Box
                                } else {
                                    focusedTrackSid = null
                                }
                            }
                            when (remoteTracks.size) {
                                0 -> {
                                    coachTrackInfo?.let { me ->
                                        StreamingCamera(
                                            track = me.track as? VideoTrack,
                                            userName = me.participantIdentity,
                                            isCameraEnabled = me.isCameraEnabled,
                                            isScreenShare = me.isScreenShare,
                                            eglBaseContext = eglBaseContext,
                                            room = room,
                                            modifier = Modifier.fillMaxSize(),
                                            onDoubleTap = { me.track?.sid?.let { focusedTrackSid = it } }
                                        )
                                    } ?: Text("비디오 스트림을 준비 중입니다.")
                                }

                                1 -> {
                                    val remote = remoteTracks.first()

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // 위: 내 화면 (코치)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                        ) {
                                            coachTrackInfo?.let { me ->
                                                StreamingCamera(
                                                    track = me.track as? VideoTrack,
                                                    userName = me.participantIdentity,
                                                    isCameraEnabled = me.isCameraEnabled,
                                                    isScreenShare = me.isScreenShare,
                                                    eglBaseContext = eglBaseContext,
                                                    room = room,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } ?: Text("내 비디오 스트림을 준비 중입니다.")
                                        }

                                        // 아래: 상대방 화면
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                        ) {
                                            StreamingCamera(
                                                track = remote.track as? VideoTrack,
                                                userName = remote.participantIdentity,
                                                isCameraEnabled = remote.isCameraEnabled,
                                                isScreenShare = remote.isScreenShare,
                                                eglBaseContext = eglBaseContext,
                                                room = room,
                                                modifier = Modifier.fillMaxSize(),
                                                onDoubleTap = { remote.track?.sid?.let { focusedTrackSid = it } }
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    val screenShareRemote = remoteTracks.firstOrNull { it.isScreenShare }
                                    if (screenShareRemote != null) {
                                        val remainingRemotes = remoteTracks.filter { it.track?.sid != screenShareRemote.track?.sid }
                                        val pageSize = 4
                                        val gridPages = if (coachTrackInfo != null) {
                                            val leftover = (remainingRemotes.size - 3).coerceAtLeast(0)
                                            1 + (if (leftover == 0) 0 else (leftover + pageSize - 1) / pageSize)
                                        } else {
                                            (remainingRemotes.size + pageSize - 1) / pageSize
                                        }

                                        LazyRow(modifier = Modifier.fillMaxSize()) {
                                            // Page 0: full-screen shared screen
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillParentMaxWidth()
                                                        .fillParentMaxHeight()
                                                        .padding(8.dp)
                                                ) {
                                                    StreamingCamera(
                                                        track = screenShareRemote.track as? VideoTrack,
                                                        userName = screenShareRemote.participantIdentity,
                                                        isCameraEnabled = screenShareRemote.isCameraEnabled,
                                                        isScreenShare = true,
                                                        eglBaseContext = eglBaseContext,
                                                        room = room,
                                                        modifier = Modifier.fillMaxSize(),
                                                        onDoubleTap = { screenShareRemote.track?.sid?.let { focusedTrackSid = it } }
                                                    )
                                                }
                                            }

                                            // Subsequent pages: 2x2 grid
                                            items(gridPages) { pageIndex ->
                                                val pageItems: List<TrackInfo> = if (pageIndex == 0) {
                                                    val list = mutableListOf<TrackInfo>()
                                                    coachTrackInfo?.let { list.add(it) }
                                                    list.addAll(remainingRemotes.take(if (coachTrackInfo != null) 3 else 4))
                                                    list
                                                } else {
                                                    val startRemote = (pageIndex - 1) * pageSize + (if (coachTrackInfo != null) 3 else 4)
                                                    val endRemote = minOf(startRemote + pageSize, remainingRemotes.size)
                                                    if (startRemote < endRemote) remainingRemotes.subList(startRemote, endRemote) else emptyList()
                                                }

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
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            for (i in 0 until 2) {
                                                                if (i < pageItems.size) {
                                                                    val t = pageItems[i]
                                                                    Box(modifier = Modifier.weight(1f)) {
                                                                        StreamingCamera(
                                                                            track = t.track as? VideoTrack,
                                                                            userName = t.participantIdentity,
                                                                            isCameraEnabled = t.isCameraEnabled,
                                                                            isScreenShare = t.isScreenShare,
                                                                            eglBaseContext = eglBaseContext,
                                                                            room = room,
                                                                            modifier = Modifier.fillMaxSize(),
                                                                            onDoubleTap = { t.track?.sid?.let { focusedTrackSid = it } }
                                                                        )
                                                                    }
                                                                } else {
                                                                    Spacer(modifier = Modifier.weight(1f))
                                                                }
                                                            }
                                                        }
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            for (i in 2 until 4) {
                                                                if (i < pageItems.size) {
                                                                    val t = pageItems[i]
                                                                    Box(modifier = Modifier.weight(1f)) {
                                                                        StreamingCamera(
                                                                            track = t.track as? VideoTrack,
                                                                            userName = t.participantIdentity,
                                                                            isCameraEnabled = t.isCameraEnabled,
                                                                            isScreenShare = t.isScreenShare,
                                                                            eglBaseContext = eglBaseContext,
                                                                            room = room,
                                                                            modifier = Modifier.fillMaxSize(),
                                                                            onDoubleTap = { t.track?.sid?.let { focusedTrackSid = it } }
                                                                        )
                                                                    }
                                                                } else {
                                                                    Spacer(modifier = Modifier.weight(1f))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        val pageSize = 4

                                        // 기존 2x2 페이징 (첫 페이지에 나 포함)
                                        val pages = if (coachTrackInfo != null) {
                                            val remainingRemotes = (remoteTracks.size - 3).coerceAtLeast(0)
                                            1 + (if (remainingRemotes == 0) 0 else (remainingRemotes + pageSize - 1) / pageSize)
                                        } else {
                                            (remoteTracks.size + pageSize - 1) / pageSize
                                        }

                                        LazyRow(
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(pages) { pageIndex ->
                                                val pageItems: List<TrackInfo> = if (pageIndex == 0) {
                                                    val list = mutableListOf<TrackInfo>()
                                                    coachTrackInfo?.let { list.add(it) }
                                                    list.addAll(remoteTracks.take(if (coachTrackInfo != null) 3 else 4))
                                                    list
                                                } else {
                                                    val startRemote = (pageIndex - 1) * pageSize + (if (coachTrackInfo != null) 3 else 4)
                                                    val endRemote = minOf(startRemote + pageSize, remoteTracks.size)
                                                    if (startRemote < endRemote) remoteTracks.subList(startRemote, endRemote) else emptyList()
                                                }

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
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            for (i in 0 until 2) {
                                                                if (i < pageItems.size) {
                                                                    val t = pageItems[i]
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
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            for (i in 2 until 4) {
                                                                if (i < pageItems.size) {
                                                                    val t = pageItems[i]
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