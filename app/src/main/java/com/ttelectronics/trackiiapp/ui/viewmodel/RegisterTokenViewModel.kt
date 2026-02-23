package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ttelectronics.trackiiapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegisterTokenUiState(
    val tokenCode: String = "",
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    val errorMessage: String? = null
)

class RegisterTokenViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterTokenUiState())
    val uiState: StateFlow<RegisterTokenUiState> = _uiState.asStateFlow()

    fun onTokenChange(value: String) = _uiState.update {
        it.copy(tokenCode = value, errorMessage = null, isValid = false)
    }

    fun validateToken() {
        val tokenCode = _uiState.value.tokenCode.trim()
        if (tokenCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "TokenCode es obligatorio", isValid = false, isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = false, errorMessage = null, isValid = true) }
    }
}

class RegisterTokenViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RegisterTokenViewModel(authRepository) as T
}
