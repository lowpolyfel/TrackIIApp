package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.local.AppSession
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
    val errorMessage: String? = null
)

class ProductAdvanceFinalReviewViewModel(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductAdvanceFinalReviewUiState())
    val uiState: StateFlow<ProductAdvanceFinalReviewUiState> = _uiState.asStateFlow()

    fun submitAll(
        lotNumber: String,
        partNumber: String,
        qtyIn: Int,
        scrap: Int,
        errorCodeId: Int,
        comments: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, isSuccess = false) }
            runCatching {
                scannerRepository.registerScan(
                    workOrderNumber = lotNumber,
                    partNumber = partNumber,
                    userId = appSession.userId,
                    deviceId = appSession.deviceId,
                    qtyIn = qtyIn
                )

                if (scrap > 0) {
                    scannerRepository.partialScrap(
                        ScrapOrderRequest(
                            workOrderNumber = lotNumber,
                            partNumber = partNumber,
                            quantity = scrap,
                            errorCodeId = errorCodeId,
                            comments = comments,
                            userId = appSession.userId,
                            deviceId = appSession.deviceId
                        )
                    )
                }
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
