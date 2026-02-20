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

data class ScannerUiState(
    val isValidating: Boolean = false,
    val validationError: String? = null,
    val isProductFound: Boolean = false,
    val shouldNavigate: Boolean = false
)

class ScannerViewModel(private val scannerRepository: ScannerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun validatePart(partNumber: String) {
        if (partNumber.isBlank()) {
            _uiState.update { it.copy(validationError = "No. de parte es obligatorio") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, validationError = null, shouldNavigate = false) }
            runCatching { scannerRepository.validatePartExists(partNumber.trim()) }
                .onSuccess { found ->
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            isProductFound = found,
                            shouldNavigate = true,
                            validationError = if (found) null else "No se encontró la orden para esta parte."
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            shouldNavigate = false,
                            validationError = ApiErrorParser.readableError(ex)
                        )
                    }
                }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(shouldNavigate = false) }
    }
}

class ScannerViewModelFactory(private val scannerRepository: ScannerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ScannerViewModel(scannerRepository) as T
}
