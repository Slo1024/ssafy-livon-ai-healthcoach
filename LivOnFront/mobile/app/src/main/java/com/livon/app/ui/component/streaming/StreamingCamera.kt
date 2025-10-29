package com.livon.app.ui.component.streaming

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.renderer.SurfaceViewRenderer
import livekit.org.webrtc.RendererCommon

@Composable
fun StreamingCamera(
    track: VideoTrack?,
    userName: String,
    isCameraEnabled: Boolean,
    eglBaseContext: livekit.org.webrtc.EglBase.Context,
    room: Room? = null,
    modifier: Modifier = Modifier
) {
    val isLocalTrack = track is LocalVideoTrack

    Box(modifier = modifier.fillMaxSize()) {
        if (track != null && isCameraEnabled) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        // Room을 통해 렌더러 초기화 (튜토리얼 방식)
                        if (room != null) {
                            try {
                                room.initVideoRenderer(this)
                                Log.d("StreamingCamera", "Renderer initialized via Room.initVideoRenderer")
                            } catch (e: Exception) {
                                Log.e("StreamingCamera", "Error initializing renderer via Room", e)
                            }
                        }
                        
                        setEnableHardwareScaler(true)
                        setMirror(isLocalTrack)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        Log.d("StreamingCamera", "Creating SurfaceViewRenderer for track: ${track.sid}, isLocal=$isLocalTrack")
                        
                        // 트랙에 렌더러 추가
                        track.addRenderer(this)
                        Log.d("StreamingCamera", "Renderer added to track: ${track.sid}")
                        
                        // 렌더러가 프레임을 받는지 확인하기 위해 약간의 delay 후 상태 확인
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            Log.d("StreamingCamera", "Renderer state after 1s - width: ${width}, height: ${height}, visibility: ${visibility}")
                        }, 1000)
                    }
                },
                update = { renderer ->
                    // track이 변경되면 렌더러 재연결
                    if (track != null) {
                        try {
                            track.removeRenderer(renderer)
                            track.addRenderer(renderer)
                            Log.d("StreamingCamera", "Renderer updated for track: ${track.sid}")
                        } catch (e: Exception) {
                            Log.e("StreamingCamera", "Error updating renderer", e)
                        }
                    }
                },
                onRelease = { view ->
                    try {
                        if (track != null) {
                            track.removeRenderer(view)
                            Log.d("StreamingCamera", "Renderer removed from track: ${track.sid}")
                        }
                        view.release()
                        Log.d("StreamingCamera", "Renderer released")
                    } catch (e: Exception) {
                        Log.e("StreamingCamera", "Error releasing renderer", e)
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("카메라 비활성화", color = Color.White)
            }
        }

        UserNameTag(
            name = userName,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

@Composable
fun UserNameTag(name: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = name,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
