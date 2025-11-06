package com.livon.app.feature.member.home.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.UserRepository
import com.livon.app.feature.member.my.MyInfoUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val info: MyInfoUiState? = null,
    val updateInProgress: Boolean = false,
    val updateError: String? = null
)

class UserViewModel(private val repo: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UserUiState(isLoading = true)
            val res = try { repo.getMyInfo() } catch (t: Throwable) { Result.failure(t) }
            if (res.isSuccess) {
                _uiState.value = UserUiState(isLoading = false, info = res.getOrNull())
            } else {
                _uiState.value = UserUiState(isLoading = false, error = res.exceptionOrNull()?.message)
            }
        }
    }

    fun updateProfileImage(profileImageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("UserViewModel", "updateProfileImage called")
            _uiState.value = _uiState.value.copy(updateInProgress = true, updateError = null)
            val res = try { repo.updateProfileImage(profileImageUrl) } catch (t: Throwable) { Result.failure<Boolean>(t) }
            if (res.isSuccess) {
                // on success, reload user info
                Log.d("UserViewModel", "updateProfileImage success, reloading user info")
                load()
                _uiState.value = _uiState.value.copy(updateInProgress = false)
            } else {
                val err = res.exceptionOrNull()?.message ?: "update failed"
                Log.d("UserViewModel", "updateProfileImage failed: $err")
                _uiState.value = _uiState.value.copy(updateInProgress = false, updateError = err)
            }
        }
    }
}
