package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val partInfo: PartLookupResponse? = null,
    val contextInfo: WorkOrderContextResponse? = null,
    val qtyInput: String = "",
    val saveSuccess: Boolean = false
)

class TaskDetailViewModel(private val scannerRepository: ScannerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    fun onQtyChange(value: String) {
        _uiState.update { it.copy(qtyInput = value.filter { ch -> ch.isDigit() }, errorMessage = null) }
    }

    fun loadData(partNumber: String, workOrderNumber: String, deviceId: Int) {
        if (partNumber.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val partResult = runCatching { scannerRepository.lookupPart(partNumber.trim()) }
            val ctxResult = runCatching { scannerRepository.getWorkOrderContext(workOrderNumber.trim(), deviceId) }
            val err = partResult.exceptionOrNull()?.let { ApiErrorParser.readableError(it) }
                ?: ctxResult.exceptionOrNull()?.let { ApiErrorParser.readableError(it) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = err,
                    partInfo = partResult.getOrNull(),
                    contextInfo = ctxResult.getOrNull()
                )
            }
        }
    }

    fun saveScan(taskType: TaskType, workOrderNumber: String, partNumber: String, deviceId: Int, deviceName: String) {
        val state = _uiState.value
        val context = state.contextInfo
        val part = state.partInfo
        val qty = state.qtyInput.toIntOrNull()

        if (part?.areaId == 1 && (context?.isFirstStep == true) && !deviceName.contains("alloy", ignoreCase = true)) {
            _uiState.update { it.copy(errorMessage = "Solo tabletas Alloy pueden abrir ordenes de Discretos.") }
            return
        }

        if (taskType == TaskType.ProductAdvance) {
            if (qty == null || qty <= 0) {
                _uiState.update { it.copy(errorMessage = "Ingresa piezas válidas.") }
                return
            }
            if (context?.canProceed == false) {
                _uiState.update { it.copy(errorMessage = context.message ?: "No se puede avanzar la orden en el paso actual.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, saveSuccess = false) }
            runCatching {
                scannerRepository.registerEntryScan(
                    workOrderNumber = workOrderNumber,
                    partNumber = partNumber,
                    deviceId = deviceId,
                    qtyIn = if (taskType == TaskType.ProductAdvance) qty else null
                )
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ApiErrorParser.readableError(ex)) }
            }
        }
    }
}

class TaskDetailViewModelFactory(private val scannerRepository: ScannerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = TaskDetailViewModel(scannerRepository) as T
}
