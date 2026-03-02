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

data class PartialScrapUiState(
    val lotNumber: String = "",
    val partNumber: String = "",
    val difference: Int = 0,
    val shouldRegister: Boolean? = null,
    val categories: List<ErrorCategoryResponse> = emptyList(),
    val codes: List<ErrorCodeResponse> = emptyList(),
    val selectedCategory: ErrorCategoryResponse? = null,
    val selectedCode: ErrorCodeResponse? = null,
    val comments: String = "",
    val isLoadingCategories: Boolean = false,
    val isLoadingCodes: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class PartialScrapViewModel(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(PartialScrapUiState())
    val uiState: StateFlow<PartialScrapUiState> = _uiState.asStateFlow()

    fun initialize(lotNumber: String, partNumber: String, difference: Int) {
        _uiState.update {
            it.copy(
                lotNumber = lotNumber,
                partNumber = partNumber,
                difference = difference,
                shouldRegister = null,
                selectedCategory = null,
                selectedCode = null,
                comments = "",
                errorMessage = null,
                saveSuccess = false,
                categories = emptyList(),
                codes = emptyList()
            )
        }
    }

    fun answerShouldRegister(shouldRegister: Boolean) {
        _uiState.update { it.copy(shouldRegister = shouldRegister, errorMessage = null) }
        if (shouldRegister && _uiState.value.categories.isEmpty()) {
            loadCategories()
        }
    }

    private fun loadCategories() {
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
                    _uiState.update { it.copy(isLoadingCategories = false, errorMessage = ApiErrorParser.readableError(ex)) }
                }
        }
    }

    fun onCategorySelected(category: ErrorCategoryResponse) {
        _uiState.update { it.copy(selectedCategory = category, selectedCode = null, codes = emptyList(), errorMessage = null) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCodes = true, errorMessage = null) }
            runCatching { scannerRepository.getErrorCodes(category.id) }
                .onSuccess { codes -> _uiState.update { it.copy(isLoadingCodes = false, codes = codes) } }
                .onFailure { ex -> _uiState.update { it.copy(isLoadingCodes = false, errorMessage = ApiErrorParser.readableError(ex)) } }
        }
    }

    fun onCodeSelected(code: ErrorCodeResponse) {
        _uiState.update { it.copy(selectedCode = code, errorMessage = null) }
    }

    fun onCommentsChange(value: String) {
        _uiState.update { it.copy(comments = value, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        val code = state.selectedCode
        if (state.shouldRegister != true) return
        if (code == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona un código de falla.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, saveSuccess = false) }
            runCatching {
                scannerRepository.partialScrap(
                    ScrapOrderRequest(
                        workOrderNumber = state.lotNumber,
                        partNumber = state.partNumber,
                        quantity = state.difference,
                        errorCodeId = code.id,
                        comments = state.comments,
                        userId = appSession.userId,
                        deviceId = appSession.deviceId
                    )
                )
            }.onSuccess { response ->
                val isSuccess = response.success == true || response.success == null
                if (isSuccess) {
                    _uiState.update { it.copy(isSubmitting = false, saveSuccess = true, errorMessage = null) }
                } else {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = response.message ?: "No fue posible registrar el scrap parcial."
                        )
                    }
                }
            }.onFailure { ex ->
                _uiState.update { it.copy(isSubmitting = false, errorMessage = ApiErrorParser.readableError(ex)) }
            }
        }
    }
}

class PartialScrapViewModelFactory(
    private val scannerRepository: ScannerRepository,
    private val appSession: AppSession
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PartialScrapViewModel(scannerRepository, appSession) as T
}
