package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapOrderRequest
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductAdvanceFinalReviewUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val partInfo: PartLookupResponse? = null
)

class ProductAdvanceFinalReviewViewModel(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductAdvanceFinalReviewUiState())
    val uiState: StateFlow<ProductAdvanceFinalReviewUiState> = _uiState.asStateFlow()

    fun loadPartInfo(partNumber: String) {
        if (partNumber.isBlank()) return
        viewModelScope.launch {
            runCatching { scannerRepository.lookupPart(partNumber.trim()) }
                .onSuccess { part ->
                    _uiState.update { it.copy(partInfo = part) }
                }
        }
    }

    fun submitAll(
        lotNumber: String,
        partNumber: String,
        qtyIn: Int, // La cantidad de piezas buenas que entran
        scrap: Int, // La cantidad de scrap que ya viene calculada de tu UI
        errorCodeId: Int,
        comments: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, isSuccess = false) }
            runCatching {

                // Enviamos ABSOLUTAMENTE TODO en una sola petición
                scannerRepository.registerScan(
                    workOrderNumber = lotNumber,
                    partNumber = partNumber,
                    userId = appSession.userId,
                    deviceId = appSession.deviceId,
                    qtyIn = qtyIn,
                    scrap = scrap,
                    // Si el scrap es 0, mandamos los errores como nulos para que la BD no marque error de llaves foráneas
                    errorCodeId = if (scrap > 0) errorCodeId else null,
                    comments = if (scrap > 0) comments else null
                )

                // NOTA: Se eliminó la llamada a partialScrap porque ahora la API
                // se encargará de procesar ambos movimientos (Avance + Scrap) a la vez.

            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true, errorMessage = null) }
            }.onFailure { ex ->
                _uiState.update { it.copy(isSubmitting = false, isSuccess = false, errorMessage = ApiErrorParser.readableError(ex)) }
            }
        }
    }
}

class ProductAdvanceFinalReviewViewModelFactory(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ProductAdvanceFinalReviewViewModel(scannerRepository, appSession) as T
}
