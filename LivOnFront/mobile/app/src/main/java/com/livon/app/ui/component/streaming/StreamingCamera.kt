package com.livon.app.ui.component.streaming

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.livon.app.R
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
    isScreenShare: Boolean = false,
    eglBaseContext: livekit.org.webrtc.EglBase.Context,
    room: Room? = null,
    modifier: Modifier = Modifier
) {
    val isLocalTrack = track is LocalVideoTrack

    Box(modifier = modifier.fillMaxSize()) {
        if (track != null && isCameraEnabled) {
            val videoModifier = Modifier.fillMaxSize()
            AndroidView(
                modifier = videoModifier,
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        if (room != null) {
                            try {
                                room.initVideoRenderer(this)
                                Log.d("StreamingCamera", "Renderer initialized via Room.initVideoRenderer")
                            } catch (e: Exception) {
                                Log.e("StreamingCamera", "Error initializing renderer via Room", e)
                            }
                        }
                        
                        setEnableHardwareScaler(true)
                        setMirror(if (isScreenShare) false else true)
                        setScalingType(
                            if (isScreenShare) RendererCommon.ScalingType.SCALE_ASPECT_FIT
                            else RendererCommon.ScalingType.SCALE_ASPECT_FILL
                        )
                        Log.d("StreamingCamera", "Creating SurfaceViewRenderer for track: ${track.sid}, isLocal=$isLocalTrack")

                        track.addRenderer(this)
                        Log.d("StreamingCamera", "Renderer added to track: ${track.sid}")

                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            Log.d("StreamingCamera", "Renderer state after 1s - width: ${width}, height: ${height}, visibility: ${visibility}")
                        }, 1000)
                    }
                },
                update = { renderer ->
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
                Icon(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "프로필",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        // 이름 태그 표시 (하단 좌측)
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
