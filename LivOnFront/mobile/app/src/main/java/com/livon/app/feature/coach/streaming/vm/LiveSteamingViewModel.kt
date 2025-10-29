//package com.livon.app.feature.coach.streaming.vm
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.livon.app.feature.shared.streaming.RoomUiState
//import com.livon.app.feature.shared.streaming.TrackInfo
//import io.livekit.android.events.RoomEvent
//import io.livekit.android.events.collect
//import io.livekit.android.room.Room
//import io.livekit.android.room.participant.LocalParticipant
//import io.livekit.android.room.track.Track
//import io.livekit.android.room.track.VideoTrack
//// ⭐️ context가 필요할 경우
//import io.livekit.android.util.getLiveKitContext // LiveKit Context 가져오는 유틸
//import io.livekit.android.room.createCameraTrack // ⭐️ Room 확장 함수 import
//
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//
//class LiveStreamingViewModel(
//    private val room: Room,
//    private val token: String,
//    private val livekitUrl: String,
//) : ViewModel() {
//
//    // ... (uiState, participantTracks 정의는 그대로 유지)
//    private val _uiState = MutableStateFlow(RoomUiState(roomName = room.name ?: "Unknown Room"))
//    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()
//
//    // ⭐️ LiveKit SDK 버전 업데이트에 맞춰 .allParticipants를 사용하거나 .remoteParticipants와 수동 결합
//    private val participantsFlow = room.remoteParticipants.map { remoteParticipants ->
//        val tracks = mutableListOf<TrackInfo>()
//
//        // 로컬 참여자 트랙 추가
//        tracks.addAll(mapParticipantTracks(room.localParticipant, isLocal = true))
//
//        // 리모트 참여자 트랙 추가
//        remoteParticipants.values.forEach { participant ->
//            tracks.addAll(mapParticipantTracks(participant, isLocal = false))
//        }
//        tracks
//    }
//
//    val participantTracks: StateFlow<List<TrackInfo>> = participantsFlow
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )
//
//    init {
//        viewModelScope.launch {
//            room.events.collect(::handleRoomEvent)
//        }
//    }
//
//    fun connect() {
//        if (room.state == Room.State.CONNECTED) return
//
//        _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
//
//        viewModelScope.launch {
//            try {
//                room.connect(
//                    url = livekitUrl,
//                    token = token,
//                    options = Room.Options(),
//                )
//                publishLocalTracks(room.localParticipant)
//
//                _uiState.value = _uiState.value.copy(isLoading = false, isConnected = true)
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    isError = true,
//                    errorMessage = e.message ?: "Failed to connect"
//                )
//            }
//        }
//    }
//
//    private fun publishLocalTracks(localParticipant: LocalParticipant) {
//        viewModelScope.launch {
//            // ⭐️ Room 확장 함수를 사용하여 트랙 생성
//            val context = room.getLiveKitContext() // Context가 필요
//
//            val cameraTrack = room.createCameraTrack(context = context)
//            localParticipant.publishVideoTrack(cameraTrack)
//
//            val micTrack = room.createAudioTrack(context = context)
//            localParticipant.publishAudioTrack(micTrack)
//        }
//    }
//
//    // ... (toggleCamera, toggleMicrophone, disconnect, onCleared는 유지)
//    fun toggleCamera() {
//        viewModelScope.launch {
//            val isEnabled = room.localParticipant.isCameraEnabled()
//            room.localParticipant.setCameraEnabled(!isEnabled)
//        }
//    }
//
//    fun toggleMicrophone() {
//        viewModelScope.launch {
//            val isEnabled = room.localParticipant.isMicrophoneEnabled()
//            room.localParticipant.setMicrophoneEnabled(!isEnabled)
//        }
//    }
//
//    fun disconnect() {
//        room.disconnect()
//    }
//
//    override fun onCleared() {
//        room.release()
//        super.onCleared()
//    }
//
//    private fun mapParticipantTracks(
//        participant: io.livekit.android.room.participant.Participant,
//        isLocal: Boolean
//    ): List<TrackInfo> {
//        // ⭐️ getTrackPublications()으로 변경
//        val tracks = participant.getTrackPublications()
//
//        return tracks.mapNotNull { publication ->
//            val track = publication.track
//            if (track != null && track is VideoTrack) {
//                TrackInfo(
//                    track = track,
//                    // ⭐️ Nullable String 처리 및 타입 맞춤
//                    participantIdentity = participant.identity ?: "Unknown",
//                    isLocal = isLocal,
//                    isCameraEnabled = participant.isCameraEnabled(),
//                    isMicrophoneEnabled = participant.isMicrophoneEnabled()
//                )
//            } else {
//                null
//            }
//        }
//    }
//
//
//    private fun handleRoomEvent(event: RoomEvent) {
//        when (event) {
//            is RoomEvent.ParticipantConnected,
//            is RoomEvent.TrackPublished,
//            is RoomEvent.TrackUnpublished,
//            is RoomEvent.TrackMuted,
//            is RoomEvent.TrackUnmuted,
//                -> {
//            }
//            is RoomEvent.Disconnected -> {
//                _uiState.value = _uiState.value.copy(isConnected = false, isLoading = false)
//            }
//            else -> {}
//        }
//    }
//}