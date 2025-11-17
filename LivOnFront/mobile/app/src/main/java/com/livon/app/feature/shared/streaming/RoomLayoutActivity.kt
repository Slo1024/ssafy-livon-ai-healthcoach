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
import io.ktor.http.HttpHeaders
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
import com.livon.app.data.session.SessionManager
import io.ktor.client.request.header
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.livon.app.data.remote.api.ConsultationVideoApi
import com.livon.app.data.remote.api.ConsultationVideoApiImpl


class RoomLayoutActivity : AppCompatActivity() {

    private val _uiState = MutableStateFlow(RoomUiState(isLoading = true))
    private val _participantTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    private val _eglBaseContext = MutableStateFlow<EglBase.Context?>(null)
    private val _room = MutableStateFlow<Room?>(null)
    private val _isSpeakerMuted = MutableStateFlow(false)

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

    private var activeEgressId: String? = null
    private var resourcesReleased: Boolean = false
    private var localParticipantNickname: String? = null

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

        eglBase = EglBase.create()
        _eglBaseContext.value = eglBase?.eglBaseContext

        room = LiveKit.create(applicationContext)
        _room.value = room


        setContent {
            val uiState = _uiState.collectAsState().value
            val participantTracks = _participantTracks.collectAsState().value
            val eglBaseContext = _eglBaseContext.collectAsState().value
            val roomState = _room.collectAsState().value
            val consultationId = intent.getLongExtra("consultationId", -1L)
            val jwtToken = SessionManager.getTokenSync() ?: ""

            if (eglBaseContext != null && roomState != null && consultationId != -1L && jwtToken.isNotEmpty()) {
                val isSpeakerMuted = _isSpeakerMuted.collectAsState().value
                LiveStreamingCoachScreen(
                    uiState = uiState,
                    participantTracks = participantTracks,
                    onLeaveRoom = ::leaveRoom,
                    eglBaseContext = eglBaseContext,
                    room = roomState,
                    onConnect = ::checkAndRequestPermissions,
                    onToggleCamera = ::toggleCamera,
                    onToggleMic = ::toggleMicrophone,
                    onShareScreen = ::toggleScreenShare,
                    consultationId = consultationId,
                    jwtToken = jwtToken,
                    onToggleSpeaker = ::toggleSpeaker,
                    isSpeakerMuted = isSpeakerMuted
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
        localParticipantNickname = participantName // 로컬 참가자 nickname 저장
        val roomName = intent.getStringExtra("roomName") ?: "Test Room"
        val consultationId = intent.getLongExtra("consultationId", -1L)

        lifecycleScope.launch {
            try {
                val token = if (consultationId != -1L) {
                    getToken(consultationId, participantName)
                } else {
                    // Fallback for old flow (JoinRoomScreen)
                    getToken(roomName, participantName)
                }

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
                            // 표시용: nickname 사용
                            val participantName = localParticipantNickname ?: localParticipant.identity?.value ?: "Participant"
                            _participantTracks.update { currentTracks ->
                                val updatedTracks = currentTracks.filter { !it.isLocal }.toMutableList()
                                updatedTracks.add(
                                    0,
                                    TrackInfo(
                                        track = selectedTrack,
                                        participantIdentity = participantName,
                                        isLocal = true,
                                        isCameraEnabled = localParticipant.isCameraEnabled(),
                                        isMicrophoneEnabled = localParticipant.isMicrophoneEnabled(),
                                        isScreenShare = screenShareTrack != null
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
            // 표시용: nickname 우선 사용 (participant.name), 없으면 identity 사용
            val participantName = event.participant.name?.takeIf { it.isNotBlank() } 
                ?: event.participant.identity?.value 
                ?: "Participant"
            Log.d("LiveKitDebug", "Remote VideoTrack subscribed: sid=${track.sid}, participant=$participantName")
            _participantTracks.update { currentTracks ->
                currentTracks + TrackInfo(
                    track = track,
                    participantIdentity = participantName,
                    isLocal = false,
                    isCameraEnabled = true,
                    isScreenShare = event.publication.source == Track.Source.SCREEN_SHARE
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
        val consultationId = intent.getLongExtra("consultationId", -1L)
        if (consultationId == -1L) {
            Log.w("LiveKitRecording", "consultationId not provided. Skipping remote recording start.")
            return
        }
        if (activeEgressId != null) {
            Log.d("LiveKitRecording", "Recording already active: $activeEgressId")
            return
        }

        lifecycleScope.launch {
            val jwtToken = SessionManager.getTokenSync()
            if (jwtToken.isNullOrBlank()) {
                Log.w("LiveKitRecording", "JWT token missing. Cannot start recording.")
                return@launch
            }
            try {
                val apiResponse = withContext(Dispatchers.IO) {
                    client.post(Urls.applicationServerUrl + "livekit/recordings/start") {
                        contentType(ContentType.Application.Json)
                        header(HttpHeaders.Authorization, "Bearer $jwtToken")
                        setBody(RemoteRecordingStartRequest(consultationId))
                    }.body<RemoteRecordingResponse<RemoteRecordingStartResult>>()
                }
                if (apiResponse.isSuccess && apiResponse.result != null) {
                    activeEgressId = apiResponse.result.egressId
                    Log.d("LiveKitRecording", "Remote recording started. egressId=$activeEgressId")
                } else {
                    Log.e("LiveKitRecording", "Failed to start remote recording: ${apiResponse.message}")
                }
            } catch (e: Exception) {
                Log.e("LiveKitRecording", "Remote recording start error", e)
            }
        }
    }

    /**
     * [수정]
     * 원격 녹화 중지 '요청'을 보냅니다.
     * 반환값이 List<RemoteRecordingFileResult>? 에서 Boolean (요청 성공 여부)로 변경되었습니다.
     */
    private suspend fun stopRemoteRecording(): Boolean {
        // activeEgressId가 없으면 요청 실패
        val egressId = activeEgressId ?: run {
            Log.w("LiveKitRecording", "activeEgressId is null. Cannot stop recording.")
            return false // [수정]
        }

        val consultationId = intent.getLongExtra("consultationId", -1L)
        if (consultationId == -1L) {
            Log.w("LiveKitRecording", "consultationId missing. Cannot stop recording.")
            activeEgressId = null // activeEgressId를 여기서 null로 처리
            return false // [수정]
        }

        val jwtToken = SessionManager.getTokenSync()
        if (jwtToken.isNullOrBlank()) {
            Log.w("LiveKitRecording", "JWT token missing. Cannot stop recording.")
            activeEgressId = null // activeEgressId를 여기서 null로 처리
            return false // [수정]
        }

        try {
            val apiResponse = withContext(Dispatchers.IO) {
                client.post(Urls.applicationServerUrl + "livekit/recordings/stop") {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $jwtToken")
                    setBody(RemoteRecordingStopRequest(consultationId, egressId))
                }.body<RemoteRecordingResponse<RemoteRecordingStopResult>>()
            }

            // API 응답이 성공이면 true 반환
            if (apiResponse.isSuccess) {
                val fileCount = apiResponse.result?.files?.size ?: 0
                // [수정] 로그 메시지 변경 (실제 중지 완료가 아니라 '요청'이 시작된 것)
                Log.d("LiveKitRecording", "Remote recording stop initiated. files=$fileCount")
                return true // [수정]
            } else {
                Log.w("LiveKitRecording", "Failed to stop remote recording: ${apiResponse.message}")
            }
        } catch (e: Exception) {
            Log.e("LiveKitRecording", "Remote recording stop error", e)
        } finally {
            // API 호출 성공 여부와 관계없이 egressId는 비워줍니다.
            activeEgressId = null
        }

        // API 실패 또는 예외 발생 시 false 반환
        return false // [수정]
    }

    override fun onDestroy() {
        runBlocking {
            try {
                stopRemoteRecording()
            } catch (_: Exception) {}
        }
        releaseResources()
        super.onDestroy()
    }

    private fun leaveRoom() {
        lifecycleScope.launch {
            try {
//                val files = stopRemoteRecording()
//                Log.d("VideoUpload", "stopRemoteRecording returned: ${files?.size ?: 0} files")
//                val firstMp4 = files?.firstOrNull { (it.location ?: "").endsWith(".mp4", ignoreCase = true) }
//                    ?: files?.firstOrNull()
//                if (firstMp4?.location != null) {
//                    Log.d("VideoUpload", "Selected file url=${firstMp4.location}")
//                    val consultationId = intent.getLongExtra("consultationId", -1L)
//                    if (consultationId != -1L) {
//                        try {
//                            Log.d("VideoUpload", "Starting uploadAndSummarizeRecordedVideo... consultationId=$consultationId")
//                            uploadAndSummarizeRecordedVideo(
//                                consultationId = consultationId,
//                                fileUrl = firstMp4.location!!,
//                                preQnA = null
//                            )
//                            Log.d("VideoUpload", "uploadAndSummarizeRecordedVideo completed")
//                        } catch (e: Exception) {
//                            Log.w("VideoUpload", "Upload and summarize failed: ${e.message}", e)
//                        }
//                    }
//                } else {
//                    Log.w("VideoUpload", "No recording file available to upload.")
//                }
                val requestSent = stopRemoteRecording()
                if (requestSent) {
                    Log.d("VideoUpload", "Stop recording request sent successfully.")
                } else {
                    Log.w("VideoUpload", "Stop recording request FAILED.")
                }
            } catch (e: Exception) {
                Log.w("RoomLayoutActivity", "leaveRoom stop/upload error: ${e.message}", e)
            } finally {
                releaseResources()
                finish()
            }
        }
    }

//    private suspend fun uploadAndSummarizeRecordedVideo(
//        consultationId: Long,
//        fileUrl: String,
//        preQnA: String?
//    ) = withContext(Dispatchers.IO) {
//        // 1) 다운로드
//        Log.d("VideoUpload", "Downloading file from $fileUrl")
//        val http = OkHttpClient()
//        val request = Request.Builder().url(fileUrl).build()
//        val response = http.newCall(request).execute()
//        if (!response.isSuccessful) {
//            throw IllegalStateException("Download failed: HTTP ${response.code}")
//        }
//        val body = response.body ?: throw IllegalStateException("Download body is null")
//        val cacheDir = cacheDir
//        val fileName = fileUrl.substringAfterLast('/').ifBlank { "recording.mp4" }
//        val tempFile = java.io.File(cacheDir, fileName)
//        body.byteStream().use { input ->
//            tempFile.outputStream().use { output ->
//                input.copyTo(output)
//            }
//        }
//        Log.d("VideoUpload", "Download complete: ${tempFile.absolutePath} (${tempFile.length()} bytes)")
//        response.close()
//
//        try {
//            // 2) 멀티파트 생성
//            val mediaType = "video/mp4".toMediaType()
//            val filePart = MultipartBody.Part.createFormData(
//                name = "file",
//                filename = tempFile.name,
//                body = tempFile.asRequestBody(mediaType)
//            )
//            val preQnAPart = preQnA?.toRequestBody("text/plain".toMediaType())
//
//            // 3) 업로드 호출
//            val api: ConsultationVideoApi = ConsultationVideoApiImpl()
//            Log.d("VideoUpload", "Calling uploadAndSummarize API...")
//            api.uploadAndSummarize(
//                consultationId = consultationId,
//                filePart = filePart,
//                preQnA = preQnAPart
//            )
//            Log.d("VideoUpload", "Upload and summarize succeeded for consultationId=$consultationId")
//        } finally {
//            // 4) 임시 파일 정리
//            runCatching { tempFile.delete() }
//            Log.d("VideoUpload", "Temp file deleted")
//        }
//    }

    private fun releaseResources() {
        if (resourcesReleased) return
        resourcesReleased = true

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

        try {
            client.close()
        } catch (e: Exception) {
            Log.w("RoomLayoutActivity", "HttpClient close error: ${e.message}")
        }
    }

    private suspend fun getToken(consultationId: Long, participantName: String): String {
        val jwtToken = SessionManager.getTokenSync()
        if (jwtToken == null) {
            throw IllegalStateException("JWT token is not available")
        }

        val response = client.post(Urls.applicationServerUrl + "token") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $jwtToken")
            setBody(TokenRequest(consultationId, participantName))
        }

        val apiResponse = response.body<TokenApiResponse>()
        if (!apiResponse.isSuccess || apiResponse.result == null) {
            throw IllegalStateException("Failed to get token: ${apiResponse.message ?: "Unknown error"}")
        }

        val token = apiResponse.result["token"]
        if (token == null) {
            throw IllegalStateException("Token not found in response result")
        }

        Log.d("token", token)
        return token
    }

    // Fallback for old flow (JoinRoomScreen) - deprecated
    private suspend fun getToken(roomName: String, participantName: String): String {
        // For backward compatibility, try to parse roomName as consultationId
        val consultationId = roomName.toLongOrNull() ?: -1L
        if (consultationId == -1L) {
            throw IllegalArgumentException("roomName must be a valid consultationId (Long)")
        }
        return getToken(consultationId, participantName)
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
        // 표시용: nickname 사용
        val participantName = localParticipantNickname ?: localParticipant.identity?.value ?: "Participant"

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
                        isMicrophoneEnabled = localParticipant.isMicrophoneEnabled(),
                        isScreenShare = false
                    )
                )
                updatedTracks.toList()
            }
        }
    }

    private fun toggleSpeaker() {
        lifecycleScope.launch {
            try {
                val isMuted = _isSpeakerMuted.value
                val newMutedState = !isMuted

                // Room의 setSpeakerMute를 사용하여 스피커 음소거 토글
                room.setSpeakerMute(newMutedState)

                _isSpeakerMuted.value = newMutedState
                Log.d("LiveKitDebug", "Speaker ${if (newMutedState) "muted" else "unmuted"}")
            } catch (e: Exception) {
                Log.e("LiveKitDebug", "Failed to toggle speaker: ${e.message}", e)
                Toast.makeText(this@RoomLayoutActivity, "스피커 제어 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

