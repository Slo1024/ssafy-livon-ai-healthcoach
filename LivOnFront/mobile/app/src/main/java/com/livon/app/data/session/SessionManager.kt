package com.livon.app.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    fun saveToken(newToken: String) {
        _token.value = newToken
    }

    fun clear() {
        _token.value = null
    }

    fun getTokenSync(): String? = _token.value
}
