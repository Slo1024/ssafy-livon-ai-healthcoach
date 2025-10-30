package com.livon.app.feature.shared.streaming

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(val participantName: String, val roomName: String)

@Serializable
data class TokenResponse(val token: String)