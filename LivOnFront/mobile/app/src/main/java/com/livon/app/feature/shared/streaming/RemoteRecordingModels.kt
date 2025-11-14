package com.livon.app.feature.shared.streaming

import kotlinx.serialization.Serializable

@Serializable
data class RemoteRecordingStartRequest(
    val consultationId: Long,
    val layout: String? = null,
    val filename: String? = null
)

@Serializable
data class RemoteRecordingStopRequest(
    val consultationId: Long,
    val egressId: String
)

@Serializable
data class RemoteRecordingStartResult(
    val egressId: String,
    val consultationId: Long,
    val sessionId: String? = null,
    val status: String? = null,
    val filePath: String? = null,
    val startedAt: Long? = null
)

@Serializable
data class RemoteRecordingStopResult(
    val egressId: String,
    val consultationId: Long,
    val status: String? = null,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val files: List<RemoteRecordingFileResult>? = null
)

@Serializable
data class RemoteRecordingFileResult(
    val filename: String? = null,
    val location: String? = null,
    val size: Long? = null,
    val duration: Long? = null
)

@Serializable
data class RemoteRecordingResponse<T>(
    val isSuccess: Boolean,
    val code: String? = null,
    val message: String? = null,
    val result: T? = null
)
