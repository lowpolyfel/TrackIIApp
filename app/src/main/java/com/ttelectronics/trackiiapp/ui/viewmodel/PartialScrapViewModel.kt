package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.core.demo.DemoMode
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
            val categories = DemoMode.demoErrorCategories()
            _uiState.update {
                it.copy(
                    isLoadingCategories = false,
                    errorMessage = null,
                    categories = categories,
                    selectedCategory = categories.firstOrNull(),
                    selectedCode = null,
                    codes = DemoMode.demoErrorCodes(categories.firstOrNull()?.id ?: 1)
                )
            }
        }
    }

    fun onCategorySelected(category: ErrorCategoryResponse) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedCategory = category,
                    selectedCode = null,
                    codes = DemoMode.demoErrorCodes(category.id),
                    isLoadingCodes = false,
                    errorMessage = null
                )
            }
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
        if (state.shouldRegister != true) return

        val fallbackCode = state.selectedCode ?: state.codes.firstOrNull() ?: DemoMode.defaultDemoErrorCode()
        val fallbackCategory = state.selectedCategory ?: state.categories.firstOrNull()

        _uiState.update {
            it.copy(
                selectedCategory = fallbackCategory,
                selectedCode = fallbackCode,
                comments = state.comments.ifBlank { "Scrap demo generado automáticamente." },
                saveSuccess = true,
                errorMessage = null
            )
        }
    }
}

class PartialScrapViewModelFactory(
    private val scannerRepository: ScannerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PartialScrapViewModel(scannerRepository) as T
}
