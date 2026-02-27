package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCategoryResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCodeResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapOrderRequest
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScrapOrderUiState(
    val isLoadingCategories: Boolean = false,
    val isLoadingCodes: Boolean = false,
    val isSubmitting: Boolean = false,
    val lotNumber: String = "",
    val partNumber: String = "",
    val selectedCategory: ErrorCategoryResponse? = null,
    val selectedCode: ErrorCodeResponse? = null,
    val qtyInput: String = "",
    val comments: String = "",
    val categories: List<ErrorCategoryResponse> = emptyList(),
    val codes: List<ErrorCodeResponse> = emptyList(),
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class ScrapOrderViewModel(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScrapOrderUiState())
    val uiState: StateFlow<ScrapOrderUiState> = _uiState.asStateFlow()

    fun initialize(lotNumber: String, partNumber: String) {
        _uiState.update {
            it.copy(
                lotNumber = lotNumber,
                partNumber = partNumber,
                qtyInput = "",
                errorMessage = null,
                saveSuccess = false
            )
        }
        loadWorkContext()
        loadCategories()
    }


    private fun loadWorkContext() {
        val state = _uiState.value
        if (state.lotNumber.isBlank() || state.partNumber.isBlank()) return

        viewModelScope.launch {
            runCatching {
                scannerRepository.getWorkOrderContext(
                    workOrderNumber = state.lotNumber,
                    deviceId = appSession.deviceId,
                    partNumber = state.partNumber
                )
            }.onSuccess { context ->
                val defaultQty = context.previousQuantity?.toString().orEmpty()
                _uiState.update {
                    if (it.qtyInput.isBlank()) it.copy(qtyInput = defaultQty) else it
                }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true, errorMessage = null) }
            runCatching { scannerRepository.getErrorCategories() }
                .onSuccess { categories ->
                    _uiState.update {
                        it.copy(
                            isLoadingCategories = false,
                            categories = categories,
                            selectedCategory = null,
                            selectedCode = null,
                            codes = emptyList()
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            isLoadingCategories = false,
                            errorMessage = ApiErrorParser.readableError(ex)
                        )
                    }
                }
        }
    }

    fun onCategorySelected(category: ErrorCategoryResponse) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedCode = null,
                codes = emptyList(),
                errorMessage = null
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCodes = true, errorMessage = null) }
            runCatching { scannerRepository.getErrorCodes(category.id) }
                .onSuccess { codes ->
                    _uiState.update {
                        it.copy(
                            isLoadingCodes = false,
                            codes = codes
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            isLoadingCodes = false,
                            errorMessage = ApiErrorParser.readableError(ex)
                        )
                    }
                }
        }
    }

    fun onCodeSelected(code: ErrorCodeResponse) {
        _uiState.update { it.copy(selectedCode = code, errorMessage = null) }
    }

    fun onQtyChange(value: String) {
        _uiState.update { it.copy(qtyInput = value.filter { ch -> ch.isDigit() }, errorMessage = null) }
    }

    fun onCommentsChange(value: String) {
        _uiState.update { it.copy(comments = value, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        val category = state.selectedCategory
        val code = state.selectedCode
        val qty = state.qtyInput.toIntOrNull()

        if (state.lotNumber.isBlank() || state.partNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Faltan datos de lote o parte.") }
            return
        }
        if (category == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona una categoría de falla.") }
            return
        }
        if (code == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona un código de falla.") }
            return
        }
        if (qty == null || qty <= 0) {
            _uiState.update { it.copy(errorMessage = "Ingresa una cantidad válida.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, saveSuccess = false) }
            runCatching {
                scannerRepository.scrapOrder(
                    ScrapOrderRequest(
                        workOrderNumber = state.lotNumber,
                        partNumber = state.partNumber,
                        quantity = qty,
                        errorCodeId = code.id,
                        comments = state.comments,
                        userId = appSession.userId,
                        deviceId = appSession.deviceId
                    )
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

class ScrapOrderViewModelFactory(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScrapOrderViewModel(scannerRepository, appSession) as T
    }
}
