package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.models.scanner.NextRouteStepResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReworkUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val lotNumber: String = "",
    val partNumber: String = "",
    val quantity: String = "",
    val reason: String = "",
    val workContext: WorkOrderContextResponse? = null,
    val locations: List<NextRouteStepResponse> = emptyList(),
    val selectedLocationId: Int? = null,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class ReworkViewModel(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReworkUiState())
    val uiState: StateFlow<ReworkUiState> = _uiState.asStateFlow()

    fun initialize(lotNumber: String, partNumber: String) {
        _uiState.update {
            it.copy(
                lotNumber = lotNumber,
                partNumber = partNumber,
                quantity = "",
                reason = "",
                errorMessage = null,
                saveSuccess = false
            )
        }
        loadContext()
    }

    fun updateQuantity(value: String) {
        _uiState.update { it.copy(quantity = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun updateReason(value: String) {
        _uiState.update { it.copy(reason = value, errorMessage = null) }
    }

    fun selectLocation(locationId: Int) {
        _uiState.update { it.copy(selectedLocationId = locationId, errorMessage = null) }
    }

    private fun loadContext() {
        val state = _uiState.value
        if (state.lotNumber.isBlank() || state.partNumber.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                scannerRepository.getWorkOrderContext(state.lotNumber, appSession.deviceId, state.partNumber)
            }.onSuccess { context ->
                val locationOptions = context.nextSteps.orEmpty().distinctBy { it.locationId }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        workContext = context,
                        quantity = context.previousQuantity?.toString().orEmpty(),
                        locations = locationOptions,
                        selectedLocationId = locationOptions.firstOrNull()?.locationId
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.readableError(ex)
                    )
                }
            }
        }
    }

    fun submitRework(isRelease: Boolean) {
        val state = _uiState.value
        val qty = state.quantity.toIntOrNull()
        val locationId = state.selectedLocationId

        if (qty == null || qty <= 0) {
            _uiState.update { it.copy(errorMessage = "Ingresa una cantidad válida.") }
            return
        }
        if (locationId == null || locationId <= 0) {
            _uiState.update { it.copy(errorMessage = "Selecciona una localidad.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, saveSuccess = false) }
            runCatching {
                scannerRepository.reworkOrder(
                    workOrderNumber = state.lotNumber,
                    partNumber = state.partNumber,
                    quantity = qty,
                    locationId = locationId,
                    isRelease = isRelease,
                    reason = state.reason.ifBlank { null },
                    userId = appSession.userId,
                    deviceId = appSession.deviceId
                )
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, saveSuccess = true) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = ApiErrorParser.readableError(ex)
                    )
                }
            }
        }
    }
}

class ReworkViewModelFactory(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReworkViewModel(scannerRepository, appSession) as T
    }
}
