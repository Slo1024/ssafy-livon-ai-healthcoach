package io.openvidu.android

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.livon.app.feature.coach.streaming.ui.LiveStreamingCoachScreen
import com.livon.app.feature.shared.streaming.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.*
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import livekit.org.webrtc.EglBase
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.os.Environment
import java.io.File
import kotlin.coroutines.cancellation.CancellationException


class RoomLayoutActivity : AppCompatActivity() {

    private val _uiState = MutableStateFlow(RoomUiState(isLoading = true))
    private val _participantTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    private val _eglBaseContext = MutableStateFlow<EglBase.Context?>(null)
    private val _room = MutableStateFlow<Room?>(null)

    private lateinit var room: Room
    private var eglBase: EglBase? = null

    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var isRecording: Boolean = false
    private var videoFile: File? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            connectToRoom()
        } else {
            Toast.makeText(this, "카메라/마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            lifecycleScope.launch {
                try {
                    room.localParticipant.setScreenShareEnabled(true, result.data)
                    isScreenSharing = true
                } catch (e: Exception) {
                    Log.e("LiveKitDebug", "Start screen share failed: ${e.message}", e)
                    Toast.makeText(this@RoomLayoutActivity, "화면 공유 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "화면 공유 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private val screenRecordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(this, ScreenRecordService::class.java).apply {
                putExtra("resultCode", result.resultCode)
                putExtra("data", result.data)
            }

        } else {
            Toast.makeText(this, "화면 녹화 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eglBase = EglBase.create()
        _eglBaseContext.value = eglBase?.eglBaseContext

        room = LiveKit.create(applicationContext)
        _room.value = room


        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        setContent {
            val uiState = _uiState.collectAsState().value
            val participantTracks = _participantTracks.collectAsState().value
            val eglBaseContext = _eglBaseContext.collectAsState().value
            val roomState = _room.collectAsState().value

            if (eglBaseContext != null && roomState != null) {
                LiveStreamingCoachScreen(
                    uiState = uiState,
                    participantTracks = participantTracks,
                    onLeaveRoom = ::leaveRoom,
                    eglBaseContext = eglBaseContext,
                    room = roomState,
                    onConnect = ::checkAndRequestPermissions,
                    onToggleCamera = ::toggleCamera,
                        onToggleMic = ::toggleMicrophone,
                        onShareScreen = ::toggleScreenShare
                )
            } else {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val neededPermissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        ).filter {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions)
        } else {
            Log.d("LiveKitDebug", "Camera & Microphone permissions already granted, connecting to room...")
            connectToRoom()
        }
    }

    private fun connectToRoom() {
        val participantName = intent.getStringExtra("participantName") ?: "Participant1"
        val roomName = intent.getStringExtra("roomName") ?: "Test Room"

        lifecycleScope.launch {
            try {
                val token = getToken(roomName, participantName)

                room.connect(
                    Urls.livekitUrl,
                    token,
                    ConnectOptions(autoSubscribe = true)
                )
                Log.d("LiveKitDebug", "Connected to room: ${room.name}, local participant: ${room.localParticipant.identity?.value}")

                startRecordingRequest()
                launch { collectRoomEvents() }
                launch { collectLocalParticipantEvents() }

                val localParticipant = room.localParticipant

                localParticipant.setMicrophoneEnabled(true)
                localParticipant.setCameraEnabled(true)
                Log.d("LiveKitDebug", "Camera enabled: ${localParticipant.isCameraEnabled()}, Microphone enabled: ${localParticipant.isMicrophoneEnabled()}")

                launch {
                    localParticipant::videoTrackPublications.flow.collect { publications ->
                        val screenShareTrack = publications.firstOrNull { it.first.source == Track.Source.SCREEN_SHARE }?.second as? VideoTrack
                        val cameraTrackPub = publications.firstOrNull { it.first.source == Track.Source.CAMERA }
                        val selectedTrack = screenShareTrack ?: (cameraTrackPub?.second as? VideoTrack)
                        Log.d("LiveKitDebug", "Video track publications updated: ${publications.size}, selected: $selectedTrack, hasScreenShare=${screenShareTrack != null}")

                        if (selectedTrack != null) {
                            val participantName = localParticipant.identity?.value ?: "Participant"
                            _participantTracks.update { currentTracks ->
                                val updatedTracks = currentTracks.filter { !it.isLocal }.toMutableList()
                                updatedTracks.add(
                                    0,
                                    TrackInfo(
                                        track = selectedTrack,
                                        participantIdentity = participantName,
                                        isLocal = true,
                                        isCameraEnabled = localParticipant.isCameraEnabled(),
                                        isMicrophoneEnabled = localParticipant.isMicrophoneEnabled()
                                    )
                                )
                                updatedTracks.toList()
                            }
                            Log.d("LiveKitDebug", "Local selected video track added: ${selectedTrack.sid}")
                        }
                    }
                }

                _uiState.update { it.copy(isLoading = false, isConnected = true, roomName = roomName) }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown Error"
                Log.e("ConnectionError", "Error connecting: $errorMessage", e)
                _uiState.update { it.copy(isLoading = false, isError = true, errorMessage = errorMessage) }
                Toast.makeText(this@RoomLayoutActivity, "Failed to join room: $errorMessage", Toast.LENGTH_LONG).show()
                leaveRoom()
            }
        }
    }


    private suspend fun collectRoomEvents() {
        Log.d("STT_INIT", "🎤 Starting to collect room events...")

        try {
            room.events.collect { event ->
                Log.v("ROOM_ALL_EVENTS", "Event: ${event::class.simpleName}")

                @OptIn(io.livekit.android.annotations.Beta::class)
                when (event) {
                    is RoomEvent.ParticipantConnected -> {
                        Log.d("LiveKitDebug", "Participant connected: ${event.participant.identity?.value}")
                    }
                    is RoomEvent.ParticipantDisconnected -> {
                        Log.d("LiveKitDebug", "Participant disconnected: ${event.participant.identity?.value}")
                    }
                    is RoomEvent.TrackPublished -> {
                        Log.d("LiveKitDebug", "Track published by ${event.participant.identity?.value}: ${event.publication.sid}")
                    }
                    is RoomEvent.TrackSubscribed -> {
                        Log.d("LiveKitDebug", "Track subscribed from ${event.participant.identity?.value}")
                        onTrackSubscribed(event)
                    }
                    is RoomEvent.TrackUnsubscribed -> {
                        Log.d("LiveKitDebug", "Track unsubscribed from ${event.participant.identity?.value}")
                        onTrackUnsubscribed(event)
                    }
                    is RoomEvent.TrackMuted -> {
                        val pub = event.publication
                        Log.d("LiveKitDebug", "Track muted: ${pub.sid}, source: ${pub.source}")
                        if (pub.source == Track.Source.CAMERA) {
                            val pid = event.participant.identity?.value
                            if (pid != null) {
                                _participantTracks.update { list ->
                                    list.map { if (!it.isLocal && it.participantIdentity == pid) it.copy(isCameraEnabled = false) else it }
                                }
                            }
                        }
                    }
                    is RoomEvent.TrackUnmuted -> {
                        val pub = event.publication
                        Log.d("LiveKitDebug", "Track unmuted: ${pub.sid}, source: ${pub.source}")
                        if (pub.source == Track.Source.CAMERA) {
                            val pid = event.participant.identity?.value
                            if (pid != null) {
                                _participantTracks.update { list ->
                                    list.map { if (!it.isLocal && it.participantIdentity == pid) it.copy(isCameraEnabled = true) else it }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        } catch (e: CancellationException) {
            Log.d("STT_INIT", "collectRoomEvents cancelled due to Activity destruction")
        }
    }



    private suspend fun collectLocalParticipantEvents() {
        try {
            room.localParticipant.events.collect { event ->
                when (event) {
                    is io.livekit.android.events.ParticipantEvent.LocalTrackPublished -> {
                        val track = event.publication.track
                        if (track is LocalAudioTrack) {
                            Log.i("LiveKit_Setup", "오디오 트랙이 성공적으로 게시되었습니다: ${track.name}")
                        } else if (track is LocalVideoTrack) {
                            Log.i("LiveKit_Setup", "비디오 트랙이 성공적으로 게시되었습니다: ${track.name}")
                        }
                        updateLocalParticipantInfo()
                    }

                    else -> {}
                }
            }
        } catch (e: CancellationException) {
            Log.d("LiveKitDebug", "collectLocalParticipantEvents cancelled")
        } catch (e: Exception) {
            Log.e("LiveKitDebug", "Error in collectLocalParticipantEvents: ${e.message}", e)
        }

    }

    private fun onTrackSubscribed(event: RoomEvent.TrackSubscribed) {
        val track = event.track
        if (track is VideoTrack) {
            Log.d("LiveKitDebug", "Remote VideoTrack subscribed: sid=${track.sid}, participant=${event.participant.identity?.value}")
            _participantTracks.update { currentTracks ->
                currentTracks + TrackInfo(
                    track = track,
                    participantIdentity = event.participant.identity!!.value,
                    isLocal = false,
                    isCameraEnabled = true
                )
            }
        }
    }

    private fun onTrackUnsubscribed(event: RoomEvent.TrackUnsubscribed) {
        val track = event.track
        if (track is VideoTrack) {
            _participantTracks.update { currentTracks ->
                currentTracks.filter { it.track?.sid != track.sid }
            }
        }
    }

    private fun startRecordingRequest() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        screenRecordLauncher.launch(intent)
    }

    private fun startScreenRecording(resultCode: Int, data: Intent) {
        if (isRecording) return

        val dm = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val density = dm.densityDpi

        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        if (!moviesDir.exists()) moviesDir.mkdirs()
        videoFile = File(moviesDir, "record_${System.currentTimeMillis()}.mp4")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile!!.absolutePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoSize(width, height)
            setVideoEncodingBitRate(5 * 1024 * 1024)
            setVideoFrameRate(30)
            prepare()
        }

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                if (isRecording) {
                    try {
                        stopScreenRecording()
                    } catch (e: RuntimeException) {
                        Log.e("ScreenRecord", "MediaRecorder stop failed: ${e.message}")
                        videoFile?.delete()
                    }
                }
            }
        }, null)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecording",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface, null, null
        )


        mediaRecorder?.start()
        isRecording = true
        Toast.makeText(this, "화면 녹화 시작!", Toast.LENGTH_SHORT).show()
    }

    private fun stopScreenRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            virtualDisplay?.release()
            mediaProjection?.stop()

            videoFile?.let { file ->
                MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null) { path, uri ->
                    Log.d("ScreenRecord", "Saved to gallery: $path")
                }
                Toast.makeText(this, "영상 저장 완료! 갤러리에서 확인하세요.", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("ScreenRecord", "Error stopping recording", e)
        } finally {
            mediaRecorder = null
            virtualDisplay = null
            mediaProjection = null
            isRecording = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopScreenRecording()

        try {
            room.disconnect()
        } catch (e: Exception) {
            Log.w("RoomLayoutActivity", "Error during room disconnect: ${e.message}")
        }

        eglBase?.let { base ->
            try {
                base.release()
                Log.d("RoomLayoutActivity", "EglBase released")
            } catch (e: RuntimeException) {
                Log.w("RoomLayoutActivity", "EglBase release failed (already released)", e)
            }
        }
        eglBase = null

        client.close()
    }

    private fun leaveRoom() {
        stopScreenRecording()

        room.disconnect()
        client.close()
        eglBase?.release()
        finish()
    }

    private suspend fun getToken(roomName: String, participantName: String): String {
        val response = client.post(Urls.applicationServerUrl + "token") {
            contentType(ContentType.Application.Json)
            setBody(TokenRequest(roomName, participantName))
        }

        Log.d("token", response.body<TokenResponse>().token)
        return response.body<TokenResponse>().token
    }

    private fun toggleCamera() {
        lifecycleScope.launch {
            val localParticipant = room.localParticipant
            localParticipant.setCameraEnabled(!localParticipant.isCameraEnabled())
            updateLocalParticipantInfo()
        }
    }
    private fun toggleMicrophone() {
        lifecycleScope.launch {
            val localParticipant = room.localParticipant
            localParticipant.setMicrophoneEnabled(!localParticipant.isMicrophoneEnabled())
            updateLocalParticipantInfo()
        }
    }

    private var isScreenSharing = false
    private fun toggleScreenShare() {
        lifecycleScope.launch {
            val localParticipant = room.localParticipant
            try {
                if (!isScreenSharing) {
                    val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    val intent = mpm.createScreenCaptureIntent()
                    screenCaptureLauncher.launch(intent)
                } else {
                    localParticipant.setScreenShareEnabled(false)
                    isScreenSharing = false
                }
            } catch (e: Exception) {
                Log.e("LiveKitDebug", "Screen share toggle failed: ${e.message}", e)
                Toast.makeText(this@RoomLayoutActivity, "화면 공유 토글 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLocalParticipantInfo() {
        val localParticipant = room.localParticipant
        val participantName = localParticipant.identity?.value ?: "Participant"

        val cameraTrackPublication = localParticipant.getTrackPublication(Track.Source.CAMERA)
        val cameraTrack = cameraTrackPublication?.track
        val isCameraEnabled = localParticipant.isCameraEnabled()

        if (cameraTrack != null) {
            _participantTracks.update { currentTracks ->
                val updatedTracks = currentTracks.filter { !it.isLocal }.toMutableList()
                updatedTracks.add(
                    0,
                    TrackInfo(
                        track = cameraTrack,
                        participantIdentity = participantName,
                        isLocal = true,
                        isCameraEnabled = isCameraEnabled,
                        isMicrophoneEnabled = localParticipant.isMicrophoneEnabled()
                    )
                )
                updatedTracks.toList()
            }
        }
    }
}
