package com.livon.app.feature.coach.streaming.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.livon.app.feature.shared.streaming.RoomUiState
import com.livon.app.feature.shared.streaming.TrackInfo
import com.livon.app.ui.component.streaming.CoachStreamingHeader
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
) {
    LaunchedEffect(key1 = true) {
        onConnect()
    }

    LivonTheme {
        Scaffold(
            topBar = {
                CoachStreamingHeader(
                    roomName = uiState.roomName,
                    onLeaveRoom = onLeaveRoom
                )
            },
            bottomBar = {
                StreamingNav(
                    //onToggleCamera = onToggleCamera,
                    //onToggleMic = onToggleMic
                )
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

                Log.d("LiveStreamingCoachScreen", "Participant tracks count: ${participantTracks.size}")
                Log.d("LiveStreamingCoachScreen", "Coach track info: $coachTrackInfo")
                Log.d("LiveStreamingCoachScreen", "Remote tracks count: ${remoteTracks.size}")

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coachTrackInfo != null) {
                            val videoTrack = coachTrackInfo.track as? VideoTrack
                            Log.d("LiveStreamingCoachScreen", "Coach video track: $videoTrack, enabled=${coachTrackInfo.isCameraEnabled}")

                            StreamingCamera(
                                track = videoTrack,
                                userName = coachTrackInfo.participantIdentity,
                                isCameraEnabled = coachTrackInfo.isCameraEnabled,
                                eglBaseContext = eglBaseContext,
                                room = room,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Log.w("LiveStreamingCoachScreen", "No coach track info found")
                            Text("비디오 스트림을 준비 중입니다.")
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        items(remoteTracks) { trackInfo ->
                            StreamingCamera(
                                track = trackInfo.track as? VideoTrack,
                                userName = trackInfo.participantIdentity,
                                isCameraEnabled = trackInfo.isCameraEnabled,
                                eglBaseContext = eglBaseContext,
                                room = room,
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}