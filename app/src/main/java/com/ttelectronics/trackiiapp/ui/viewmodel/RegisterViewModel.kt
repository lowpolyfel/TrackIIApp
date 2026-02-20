package com.ttelectronics.trackiiapp.ui.viewmodel

import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.models.auth.LocationDto
import com.ttelectronics.trackiiapp.data.models.auth.RegisterRequest
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val tokenCode: String = "",
    val deviceUid: String = "",
    val deviceName: String = "",
    val username: String = "",
    val password: String = "",
    val locations: List<LocationDto> = emptyList(),
    val selectedLocationId: UInt? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun setToken(tokenCode: String) = _uiState.update { it.copy(tokenCode = tokenCode, errorMessage = null) }
    fun setDeviceUid(uid: String) = _uiState.update { it.copy(deviceUid = uid) }
    fun onDeviceNameChange(value: String) = _uiState.update { it.copy(deviceName = value, errorMessage = null) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onLocationChange(locationId: UInt) = _uiState.update { it.copy(selectedLocationId = locationId, errorMessage = null) }

    fun preloadDeviceUid(androidId: String?) {
        if (_uiState.value.deviceUid.isBlank()) {
            setDeviceUid(androidId ?: Settings.Secure.ANDROID_ID)
        }
    }

    fun loadLocations() {
        if (_uiState.value.locations.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { authRepository.getLocations() }
                .onSuccess { locations ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            locations = locations,
                            selectedLocationId = locations.firstOrNull()?.id
                        )
                    }
                }
                .onFailure { ex -> _uiState.update { it.copy(isLoading = false, errorMessage = ApiErrorParser.readableError(ex)) } }
        }
    }

    fun register() {
        val state = _uiState.value
        when {
            state.tokenCode.isBlank() -> _uiState.update { it.copy(errorMessage = "TokenCode es obligatorio.") }
            state.username.isBlank() -> _uiState.update { it.copy(errorMessage = "Usuario es obligatorio.") }
            state.password.isBlank() -> _uiState.update { it.copy(errorMessage = "Contraseña es obligatoria.") }
            state.deviceName.isBlank() -> _uiState.update { it.copy(errorMessage = "Nombre de tableta es obligatorio.") }
            state.selectedLocationId == null -> _uiState.update { it.copy(errorMessage = "Localidad es obligatoria.") }
            else -> createAccount(state)
        }
    }

    private fun createAccount(state: RegisterUiState) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                authRepository.register(
                    RegisterRequest(
                        username = state.username.trim(),
                        password = state.password,
                        tokenCode = state.tokenCode.trim(),
                        locationId = state.selectedLocationId!!,
                        deviceUid = state.deviceUid,
                        deviceName = state.deviceName.trim()
                    )
                )
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = response.message.ifBlank { "Registro completado" }
                    )
                }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ApiErrorParser.readableError(ex)) }
            }
        }
    }
}

class RegisterViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RegisterViewModel(authRepository) as T
}
