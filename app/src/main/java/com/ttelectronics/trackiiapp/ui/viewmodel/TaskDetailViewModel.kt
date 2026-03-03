package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.data.models.auth.LocationDto
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ReworkResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.AuthRepository
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import com.ttelectronics.trackiiapp.domain.scanner.ProductAdvanceScanPolicy
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
    val reworkReason: String = "",
    val reworkLocations: List<LocationDto> = emptyList(),
    val selectedReworkLocation: LocationDto? = null,
    val isSubmitting: Boolean = false,
    val saveSuccess: Boolean = false,
    val piecesDifference: Int = 0
)


class TaskDetailViewModel(
    private val scannerRepository: ScannerRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    private val productAdvanceScanPolicy = ProductAdvanceScanPolicy()

    fun onQtyChange(value: String) {
        _uiState.update { it.copy(qtyInput = value.filter { ch -> ch.isDigit() }, errorMessage = null) }
    }

    fun onReworkReasonChange(value: String) {
        _uiState.update { it.copy(reworkReason = value, errorMessage = null) }
    }

    fun onReworkLocationSelected(locationName: String) {
        val selected = _uiState.value.reworkLocations.firstOrNull { it.name == locationName }
        _uiState.update { it.copy(selectedReworkLocation = selected, errorMessage = null) }
    }

    fun loadData(partNumber: String, workOrderNumber: String, deviceId: Int) {
        if (partNumber.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    partInfo = null,
                    contextInfo = null,
                    saveSuccess = false,
                    piecesDifference = 0,
                    isSubmitting = false
                )
            }
            val partResult = runCatching { scannerRepository.lookupPart(partNumber.trim()) }
            val ctxResult = runCatching { scannerRepository.getWorkOrderContext(workOrderNumber.trim(), deviceId, partNumber.trim()) }
            val locationResult = runCatching { authRepository.getLocations() }
            val err = partResult.exceptionOrNull()?.let { ApiErrorParser.readableError(it) }
                ?: ctxResult.exceptionOrNull()?.let { ApiErrorParser.readableError(it) }
                ?: locationResult.exceptionOrNull()?.let { ApiErrorParser.readableError(it) }

            val normalizedContext = ctxResult.getOrNull()?.let { context ->
                if (context.isNew == true) {
                    context.copy(previousQuantity = 0)
                } else {
                    context
                }
            }

            _uiState.update {
                val locations = locationResult.getOrNull().orEmpty()
                val selectedLocation = it.selectedReworkLocation
                    ?.let { selected -> locations.firstOrNull { location -> location.id == selected.id } }
                    ?: locations.firstOrNull()
                it.copy(
                    isLoading = false,
                    errorMessage = err,
                    partInfo = partResult.getOrNull(),
                    contextInfo = normalizedContext,
                    reworkLocations = locations,
                    selectedReworkLocation = selectedLocation
                )
            }
        }
    }

    fun saveScan(
        taskType: TaskType,
        workOrderNumber: String,
        partNumber: String,
        userId: Int,
        deviceId: Int,
        locationName: String
    ) {
        val state = _uiState.value

        // 1. REGLA DE ORO: Prevenir el doble toque (Double Submit)
        if (state.isLoading || state.isSubmitting || state.saveSuccess) {
            return
        }

        val qtyFromInput = state.qtyInput.toIntOrNull()
        val decision = if (taskType == TaskType.ProductAdvance) {
            productAdvanceScanPolicy.evaluate(
                workOrderNumber = workOrderNumber,
                qtyInput = state.qtyInput,
                locationName = locationName,
                partInfo = state.partInfo,
                context = state.contextInfo
            )
        } else {
            null
        }

        if (taskType == TaskType.ProductAdvance && decision?.canRegister == false) {
            _uiState.update { it.copy(errorMessage = decision.localMessage) }
            return
        }

        if (taskType == TaskType.Rework) {
            val selectedLocationId = state.selectedReworkLocation?.id ?: 0
            if ((qtyFromInput ?: 0) <= 0) {
                _uiState.update { it.copy(errorMessage = "Ingresa una cantidad válida para retrabajo.") }
                return
            }
            if (selectedLocationId <= 0) {
                _uiState.update { it.copy(errorMessage = "Selecciona la localidad de retrabajo.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSubmitting = true, errorMessage = null, saveSuccess = false, piecesDifference = 0) }
            runCatching {
                if (taskType == TaskType.Rework) {
                    scannerRepository.reworkOrder(
                        workOrderNumber = workOrderNumber,
                        partNumber = partNumber,
                        quantity = qtyFromInput ?: 0,
                        locationId = state.selectedReworkLocation?.id ?: 0,
                        isRelease = false,
                        reason = state.reworkReason.ifBlank { null },
                        userId = userId,
                        deviceId = deviceId
                    )
                } else {
                    scannerRepository.registerScan(
                        workOrderNumber = workOrderNumber,
                        partNumber = partNumber,
                        userId = userId,
                        deviceId = deviceId,
                        qtyIn = if (taskType == TaskType.ProductAdvance) (decision?.qtyIn ?: 0) else (qtyFromInput ?: 0)
                    )
                }
            }.onSuccess { response ->
                val responseSuccess = when (response) {
                    // Si llegó aquí sin lanzar excepción, el registro de avance fue exitoso
                    is RegisterScanResponse -> true
                    is ReworkResponse -> response.success == true
                    else -> true
                }
                val responseMessage = when (response) {
                    is RegisterScanResponse -> response.message
                    is ReworkResponse -> response.message
                    else -> null
                }

                if (!responseSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSubmitting = false,
                            errorMessage = responseMessage ?: "No fue posible guardar el registro."
                        )
                    }
                    return@onSuccess
                }

                val previousQty = state.contextInfo?.previousQuantity ?: 0
                val inputQty = qtyFromInput ?: 0
                val diff = if (taskType == TaskType.ProductAdvance) previousQty - inputQty else 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSubmitting = false,
                        saveSuccess = true,
                        piecesDifference = if (diff > 0) diff else 0
                    )
                }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, isSubmitting = false, errorMessage = ApiErrorParser.readableError(ex)) }
            }
        }
    }

}

class TaskDetailViewModelFactory(
    private val scannerRepository: ScannerRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = TaskDetailViewModel(scannerRepository, authRepository) as T
}
