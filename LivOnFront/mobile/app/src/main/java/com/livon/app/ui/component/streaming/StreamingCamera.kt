package com.livon.app.ui.component.streaming

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.CircleShape
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
    eglBaseContext: livekit.org.webrtc.EglBase.Context,
    room: Room? = null,
    modifier: Modifier = Modifier
) {
    val isLocalTrack = track is LocalVideoTrack
    var showMemberDetail by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (track != null && isCameraEnabled) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
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
                        setMirror(isLocalTrack)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
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

        // Information icon for remote participants only
        if (!isLocalTrack) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(2f)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { showMemberDetail = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "정보",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        UserNameTag(
            name = userName,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(2f)
                .padding(12.dp)
        )

        // StreamingMemberDetail overlay
        if (showMemberDetail && !isLocalTrack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                StreamingMemberDetail(
                    memberName = userName,
                    bioInfoList = listOf(
                        BioInfo("키"),
                        BioInfo("몸무게"),
                        BioInfo("운동 경력")
                    ),
                    bottomText = "입력하신 정보는 공개되지 않습니다.",
                    qaList = listOf(
                        QAItem("운동 목표는 무엇인가요?"),
                        QAItem("선호하는 운동 시간대는?")
                    ),
                    onClose = { showMemberDetail = false },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
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
