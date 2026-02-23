package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            _uiState.update { it.copy(errorMessage = "TokenCode es obligatorio") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isValid = false) }
            runCatching { authRepository.validateToken(tokenCode) }
                .onSuccess { valid ->
                    if (valid) {
                        _uiState.update { it.copy(isLoading = false, isValid = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Token inválido") }
                    }
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = ApiErrorParser.readableError(ex)) }
                }
        }
    }
}

class RegisterTokenViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RegisterTokenViewModel(authRepository) as T
}
