package com.livon.app.feature.coach.streaming.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.track.VideoTrack

@Composable
fun ParticipantVideoView(
    track: VideoTrack?,
    identity: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.aspectRatio(3f / 4f)) { // XML의 DimensionRatio="3:4" 반영

        if (track != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        setEnableHardwareScaler(true)
                        track.addRenderer(this)
                    }
                },
                onRelease = { view ->
                    track.removeRenderer(view)
                    view.release()
                }
            )
        } else {
            // 트랙이 로드되지 않았을 때 대체 화면
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(identity, color = Color.White)
            }
        }

        // 참가자 이름 표시
        Text(
            text = identity,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f)) // 배경 스타일 추가
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}