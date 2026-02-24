package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanRequest
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanReviewUiState(
    val isLoading: Boolean = false,
    val isNewOrder: Boolean = true,
    val scannedPartNumber: String = "",
    val quantityInput: String = "",
    val stepInfoText: String = "Consultando información...",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ScanReviewViewModel(
    private val scannerRepository: ScannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanReviewUiState())
    val uiState: StateFlow<ScanReviewUiState> = _uiState.asStateFlow()

    fun fetchOrderContext(partNumber: String, workOrderNumber: String, deviceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, scannedPartNumber = partNumber, errorMessage = null) }

            try {
                val context = scannerRepository.getWorkOrderContext(workOrderNumber, deviceId)

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isNewOrder = context.isNew,
                        quantityInput = if (!context.isNew) context.previousQuantity?.toString() ?: "" else "",
                        stepInfoText = if (context.isNew) {
                            "Paso 1 (Nueva Orden)"
                        } else {
                            "Paso actual: ${context.currentStepName} | Siguiente: ${context.nextSteps.firstOrNull() ?: "Fin de ruta"}"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.parse(e) ?: "Error de conexión"
                    )
                }
            }
        }
    }

    fun updateQuantity(newQty: String) {
        if (newQty.all { it.isDigit() }) {
            _uiState.update { it.copy(quantityInput = newQty) }
        }
    }

    fun registerScan(workOrderNumber: String, deviceId: Int) {
        val qty = _uiState.value.quantityInput.toIntOrNull()
        if (qty == null || qty <= 0) {
            _uiState.update { it.copy(errorMessage = "Ingrese una cantidad válida") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val request = RegisterScanRequest(
                    workOrderNumber = workOrderNumber,
                    partNumber = _uiState.value.scannedPartNumber,
                    quantity = qty,
                    deviceId = deviceId,
                    isAlloyTablet = false
                )
                val response = scannerRepository.registerScan(request)

                _uiState.update { it.copy(isLoading = false, successMessage = response.message) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.parse(e) ?: "Error al registrar"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

class ScanReviewViewModelFactory(private val scannerRepository: ScannerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ScanReviewViewModel(scannerRepository) as T
}
