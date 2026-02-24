package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
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
    val partInfo: PartLookupResponse? = null,
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

            val partResult = runCatching { scannerRepository.lookupPart(partNumber) }
            val contextResult = runCatching { scannerRepository.getWorkOrderContext(workOrderNumber, deviceId) }

            val partInfo = partResult.getOrNull()
            val context = contextResult.getOrNull()
            val nextStep = context?.nextSteps?.firstOrNull() ?: context?.nextLocationName ?: "Fin de ruta"
            val currentStep = context?.currentStepName ?: context?.currentStepNumber?.toString().orEmpty()
            val isNew = context?.isNew ?: true

            val firstError = partResult.exceptionOrNull() ?: contextResult.exceptionOrNull()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    partInfo = partInfo,
                    isNewOrder = isNew,
                    quantityInput = if (!isNew) context?.previousQuantity?.toString() ?: "" else "",
                    stepInfoText = if (isNew) {
                        "Paso 1 (Nueva Orden)"
                    } else {
                        "Paso actual: ${currentStep.ifBlank { "N/A" }} | Siguiente: $nextStep"
                    },
                    errorMessage = firstError?.let { ApiErrorParser.readableError(it) }
                )
            }
        }
    }

    fun updateQuantity(newQty: String) {
        if (newQty.all { it.isDigit() }) {
            _uiState.update { it.copy(quantityInput = newQty) }
        }
    }

    fun registerScan(workOrderNumber: String, deviceId: Int, isAlloyTablet: Boolean) {
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
                    isAlloyTablet = isAlloyTablet,
                    qtyIn = qty,
                    scanType = "ENTRY"
                )
                val response = scannerRepository.registerScan(request)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = response.message ?: "Registro exitoso"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.readableError(e)
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
