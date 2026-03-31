package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCategoryResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCodeResponse
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
    private val scannerRepository: ScannerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PartialScrapUiState())
    val uiState: StateFlow<PartialScrapUiState> = _uiState.asStateFlow()

    fun initialize(lotNumber: String, partNumber: String, difference: Int) {
        _uiState.update {
            it.copy(
                lotNumber = lotNumber,
                partNumber = partNumber,
                difference = difference,
                shouldRegister = true,
                selectedCategory = null,
                selectedCode = null,
                comments = "",
                errorMessage = null,
                saveSuccess = false

            )
        }

        loadCategories()
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

    fun applyQuickCause(isPowerOutage: Boolean) {
        viewModelScope.launch {
            val targetCategoryName = "Causa Externa"
            val targetCode = if (isPowerOutage) "X001" else "X002"
            val causeDesc = if (isPowerOutage) "se fue la luz en la planta" else "Fallo de la maquina o equipo"

            // 1. Buscar la categoría en la lista cargada
            val category = _uiState.value.categories.find { it.name.contains(targetCategoryName, ignoreCase = true) }

            if (category != null) {
                // 2. Seleccionar categoría y descargar sus códigos
                onCategorySelected(category)

                val result = runCatching { scannerRepository.getErrorCodes(category.id) }
                if (result.isSuccess) {
                    val codes = result.getOrNull() ?: emptyList()
                    _uiState.update { it.copy(isLoadingCodes = false, codes = codes) }

                    // 3. Buscar el código específico
                    val code = codes.find { it.code.contains(targetCode, ignoreCase = true) }
                    if (code != null) {
                        onCodeSelected(code)

                        // 4. Autollenar comentarios con fecha y hora
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", java.util.Locale.getDefault())
                        val currentDate = sdf.format(java.util.Date())
                        val autoComment = "Se perdio material ($targetCode - $causeDesc) el dia $currentDate"

                        onCommentsChange(autoComment)
                    }
                }
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.shouldRegister != true) return
        if (state.selectedCode == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona un código de falla.") }
            return
        }

        if (state.comments.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Los comentarios son obligatorios.") }
            return
        }

        _uiState.update { it.copy(saveSuccess = true, errorMessage = null) }
    }
}

class PartialScrapViewModelFactory(
    private val scannerRepository: ScannerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PartialScrapViewModel(scannerRepository) as T
}
