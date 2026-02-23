package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanReviewUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val partInfo: PartLookupResponse? = null,
    val contextInfo: WorkOrderContextResponse? = null
)

class ScanReviewViewModel(private val scannerRepository: ScannerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanReviewUiState())
    val uiState: StateFlow<ScanReviewUiState> = _uiState.asStateFlow()

    fun loadData(partNumber: String, workOrderNumber: String, deviceId: Int) {
        if (partNumber.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val partResult = runCatching { scannerRepository.lookupPart(partNumber) }
            val contextResult = runCatching {
                if (workOrderNumber.isBlank()) null
                else scannerRepository.getWorkOrderContext(workOrderNumber, deviceId)
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
}

class ScanReviewViewModelFactory(private val scannerRepository: ScannerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ScanReviewViewModel(scannerRepository) as T
}
