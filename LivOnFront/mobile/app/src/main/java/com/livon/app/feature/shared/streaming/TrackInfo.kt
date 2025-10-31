package com.livon.app.feature.shared.streaming

import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack

data class TrackInfo(
    val track: Track?,
    val participantIdentity: String,
    val isLocal: Boolean = false,
    val isCameraEnabled: Boolean = false,
    val isMicrophoneEnabled: Boolean = false
) {
    val videoTrack: VideoTrack?
        get() = track as? VideoTrack
}