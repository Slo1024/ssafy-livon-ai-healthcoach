package com.livon.app.feature.member.auth.vm


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.data.session.SessionManager
import com.livon.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(val isLoading: Boolean = false, val error: String? = null, val success: Boolean = false)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = AuthUiState(isLoading = true)
            val res = try {
                repo.login(email, password)
            } catch (t: Throwable) {
                Result.failure<Any>(t)
            }

            if (res.isSuccess) {
                val body = res.getOrNull() as? com.livon.app.data.remote.api.SignInResult
                val token = body?.accessToken
                if (!token.isNullOrBlank()) {
                    SessionManager.saveToken(token)
                    _state.value = AuthUiState(isLoading = false, success = true)
                    return@launch
                }
            }

            // No dev fallback: surface the failure to UI
            _state.value = AuthUiState(isLoading = false, error = (res.exceptionOrNull()?.message ?: "로그인 실패"))
        }
    }
}
