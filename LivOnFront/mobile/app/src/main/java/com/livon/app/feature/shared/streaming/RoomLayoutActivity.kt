package io.openvidu.android

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.app.NotificationManager
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
// removed unused options imports

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // EglBase 초기화
        eglBase = EglBase.create()
        _eglBaseContext.value = eglBase?.eglBaseContext

        // Room 객체 생성
        room = LiveKit.create(applicationContext)
        _room.value = room

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
                // Ensure we auto-subscribe to remote tracks
                room.connect(
                    Urls.livekitUrl,
                    token,
                    ConnectOptions(autoSubscribe = true)
                )
                Log.d("LiveKitDebug", "Connected to room: ${room.name}, local participant: ${room.localParticipant.identity?.value}")

                // 이벤트 수집 시작
                launch { collectRoomEvents() }

                val localParticipant = room.localParticipant

                // 카메라와 마이크 활성화
                localParticipant.setMicrophoneEnabled(true)
                localParticipant.setCameraEnabled(true)
                Log.d("LiveKitDebug", "Camera enabled: ${localParticipant.isCameraEnabled()}, Microphone enabled: ${localParticipant.isMicrophoneEnabled()}")

                // 로컬 비디오 트랙을 flow로 모니터링 (튜토리얼 방식)
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
        room.events.collect { event ->
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
                    is RoomEvent.TrackSubscribed -> onTrackSubscribed(event)
                is RoomEvent.TrackUnsubscribed -> onTrackUnsubscribed(event)
                    is RoomEvent.TrackMuted -> {
                        val pub = event.publication
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
    }

    private suspend fun collectLocalParticipantEvents() {
        room.localParticipant.events.collect { event ->
            when (event) {
                is io.livekit.android.events.ParticipantEvent.LocalTrackPublished -> {
                    updateLocalParticipantInfo()
                }
                else -> {}
            }
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

    private fun leaveRoom() {
        room.disconnect()
        client.close()
        eglBase?.release()
        finish()
    }

    private suspend fun getToken(roomName: String, participantName: String): String {
        val response = client.post(Urls.applicationServerUrl + "token") {
            contentType(ContentType.Application.Json)
            setBody(TokenRequest(participantName, roomName))
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
