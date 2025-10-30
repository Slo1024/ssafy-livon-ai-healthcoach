package com.livon.app.feature.shared.streaming

data class RoomUiState(
    val roomName: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)