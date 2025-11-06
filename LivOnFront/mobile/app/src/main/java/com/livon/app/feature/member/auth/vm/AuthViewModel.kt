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
            val res = repo.login(email, password)
            if (res.isSuccess) {
                val body = res.getOrNull()
                body?.accessToken?.let { SessionManager.saveToken(it) }
                _state.value = AuthUiState(isLoading = false, success = true)
            } else {
                _state.value = AuthUiState(isLoading = false, error = res.exceptionOrNull()?.message ?: "로그인 실패")
            }
        }
    }
}
