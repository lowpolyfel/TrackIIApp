package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReworkReleaseUiState(
    val isSubmitting: Boolean = false,
    val message: String? = null,
    val releaseSuccess: Boolean = false
)

class ReworkReleaseViewModel(
    private val scannerRepository: ScannerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReworkReleaseUiState())
    val uiState: StateFlow<ReworkReleaseUiState> = _uiState.asStateFlow()

    fun release(workOrderNumber: String) {
        if (workOrderNumber.isBlank()) {
            _uiState.update { it.copy(message = "No se recibió el número de lote.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null, releaseSuccess = false) }
            runCatching { scannerRepository.releaseWipItem(workOrderNumber) }
                .onSuccess { response ->
                    val isSuccess = response.success == true || response.success == null
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            releaseSuccess = isSuccess,
                            message = response.message ?: if (isSuccess) "Producto liberado correctamente." else "No fue posible liberar el producto."
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            releaseSuccess = false,
                            message = ApiErrorParser.readableError(ex)
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

class ReworkReleaseViewModelFactory(
    private val scannerRepository: ScannerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReworkReleaseViewModel(scannerRepository) as T
    }
}
