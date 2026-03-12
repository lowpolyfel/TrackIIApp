package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapOrderRequest
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanReviewUiState(
    // Estados de tu versión original (para cargar datos)
    val isLoading: Boolean = false,
    val partInfo: PartLookupResponse? = null,
    val contextInfo: WorkOrderContextResponse? = null,

    // Estados nuevos (para el guardado final)
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ScanReviewViewModel(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession // ¡Importante! Lo agregamos para poder leer el userId y deviceId
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanReviewUiState())
    val uiState: StateFlow<ScanReviewUiState> = _uiState.asStateFlow()

    // 1. TU FUNCIÓN ORIGINAL (Mantenida para cuando quieras cargar info de validación)
    fun loadData(partNumber: String, workOrderNumber: String, deviceId: Int) {
        if (partNumber.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val partResult = runCatching { scannerRepository.lookupPart(partNumber) }
            val contextResult = runCatching {
                if (workOrderNumber.isBlank()) null
                else scannerRepository.getWorkOrderContext(workOrderNumber, deviceId, partNumber)
            }

            val error = partResult.exceptionOrNull()?.let { ApiErrorParser.readableError(it) }
                ?: contextResult.exceptionOrNull()?.let { ApiErrorParser.readableError(it) }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error,
                    partInfo = partResult.getOrNull(),
                    contextInfo = contextResult.getOrNull()
                )
            }
        }
    }

    // 2. NUEVA FUNCIÓN DE SUBIDA FINAL
    fun submitAll(
        lotNumber: String,
        partNumber: String,
        qtyIn: Int,
        hasScrap: Boolean,
        difference: Int,
        errorCodeId: Int,
        scrapComments: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            try {
                // 1. Registrar la orden principal
                val scanResponse = scannerRepository.registerScan(
                    workOrderNumber = lotNumber,
                    partNumber = partNumber,
                    userId = appSession.userId,
                    deviceId = appSession.deviceId,
                    qtyIn = qtyIn
                )

                if (scanResponse.success != true) {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = scanResponse.message ?: "Error al registrar la orden.") }
                    return@launch
                }

                // 2. Si hay scrap, registrarlo justo después
                if (hasScrap && difference > 0 && errorCodeId > 0) {
                    val scrapResponse = scannerRepository.partialScrap(
                        ScrapOrderRequest(
                            workOrderNumber = lotNumber,
                            partNumber = partNumber,
                            quantity = difference,
                            errorCodeId = errorCodeId,
                            comments = scrapComments,
                            userId = appSession.userId,
                            deviceId = appSession.deviceId
                        )
                    )

                    if (scrapResponse.success == false || scrapResponse.success == null) {
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = scrapResponse.message ?: "La orden pasó, pero hubo un error registrando el scrap.") }
                        return@launch
                    }
                }

                // Todo se guardó con éxito en bloque
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }

            } catch (ex: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = ApiErrorParser.readableError(ex)) }
            }
        }
    }
}

// Actualizamos el Factory para que acepte el AppSession
class ScanReviewViewModelFactory(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ScanReviewViewModel(scannerRepository, appSession) as T
}