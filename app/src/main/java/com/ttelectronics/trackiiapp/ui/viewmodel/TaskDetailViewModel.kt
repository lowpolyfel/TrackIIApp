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
    val piecesDifference: Int = 0,
    val pendingQtyIn: Int = 0,
    val pendingReady: Boolean = false,
    val showQtyErrorOverlay: Boolean = false,
    val qtyErrorText: String = ""
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

    fun onProductAdvanceQtyChange(value: String) {
        val digits = value.filter { ch -> ch.isDigit() }
        // Ya no limitamos mientras escribe, permitimos el texto libre para validar al final
        _uiState.update { it.copy(qtyInput = digits, errorMessage = null) }
    }

    fun onProductAdvanceSliderChange(value: Float) {
        val isNewOrder = _uiState.value.contextInfo?.isNew == true
        val maxQty = if (isNewOrder) 20000 else (_uiState.value.contextInfo?.previousQuantity ?: 0).coerceAtLeast(0)
        val boundedQty = value.toInt().coerceIn(0, maxQty)
        _uiState.update { it.copy(qtyInput = boundedQty.toString(), errorMessage = null) }
    }

    fun adjustProductAdvanceQty(delta: Int) {
        val isNewOrder = _uiState.value.contextInfo?.isNew == true
        val maxQty = if (isNewOrder) 20000 else (_uiState.value.contextInfo?.previousQuantity ?: 0).coerceAtLeast(0)
        val currentQty = _uiState.value.qtyInput.toIntOrNull() ?: 0
        val boundedQty = (currentQty + delta).coerceIn(0, maxQty)
        _uiState.update { it.copy(qtyInput = boundedQty.toString(), errorMessage = null) }
    }

    // Valida la cantidad al presionar Enter o Guardar
    fun validateManualQty(): Boolean {
        val state = _uiState.value
        val typedQty = state.qtyInput.toIntOrNull() ?: 0
        val isNewOrder = state.contextInfo?.isNew == true
        val maxQty = if (isNewOrder) 20000 else (state.contextInfo?.previousQuantity ?: 0).coerceAtLeast(0)

        if (typedQty > maxQty) {
            _uiState.update {
                it.copy(
                    qtyInput = maxQty.toString(), // Autocorrige a la cantidad máxima permitida
                    showQtyErrorOverlay = true,
                    qtyErrorText = if (isNewOrder) "Cantidad inválida.\nEl máximo permitido es 20,000 piezas."
                    else "Cantidad inválida.\nEl máximo es $maxQty piezas del paso anterior."
                )
            }
            return false
        }

        // Si lo dejaron en blanco, lo restauramos a 0 o al máximo
        if (state.qtyInput.isBlank()) {
            _uiState.update { it.copy(qtyInput = if (isNewOrder) "0" else maxQty.toString()) }
        }
        return true
    }

    fun dismissQtyError() {
        _uiState.update { it.copy(showQtyErrorOverlay = false) }
    }

    fun ensureDefaultQtyFromPrevious() {
        val previousQty = (_uiState.value.contextInfo?.previousQuantity ?: 0).coerceAtLeast(0)
        if (_uiState.value.qtyInput.isBlank()) {
            _uiState.update { it.copy(qtyInput = previousQty.toString()) }
        }
    }

    fun setInitialQtyInput(value: String) {
        if (value.isBlank()) return
        _uiState.update {
            if (it.qtyInput.isBlank()) {
                val maxQty = it.contextInfo?.previousQuantity?.coerceAtLeast(0) ?: Int.MAX_VALUE
                val parsed = value.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0
                it.copy(qtyInput = parsed.coerceAtMost(maxQty).toString())
            } else {
                it
            }
        }
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
                    qtyInput = "",
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

            _uiState.update {
                val locations = locationResult.getOrNull().orEmpty()
                val selectedLocation = it.selectedReworkLocation
                    ?.let { selected -> locations.firstOrNull { location -> location.id == selected.id } }
                    ?: locations.firstOrNull()

                // Extraemos la cantidad máxima de piezas que arrojó el API
                val fetchedContext = ctxResult.getOrNull()
                val initialQty = (fetchedContext?.previousQuantity ?: 0).coerceAtLeast(0).toString()

                it.copy(
                    isLoading = false,
                    errorMessage = err,
                    partInfo = partResult.getOrNull(),
                    contextInfo = fetchedContext,
                    reworkLocations = locations,
                    selectedReworkLocation = selectedLocation,
                    // Asignamos el valor máximo por defecto al input
                    qtyInput = if (initialQty == "0" && it.qtyInput.isNotBlank()) it.qtyInput else initialQty
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
                    // AQUÍ ESTABA EL ERROR: Agregamos los parámetros faltantes en 0/null
                    scannerRepository.registerScan(
                        workOrderNumber = workOrderNumber,
                        partNumber = partNumber,
                        userId = userId,
                        deviceId = deviceId,
                        qtyIn = if (taskType == TaskType.ProductAdvance) (decision?.qtyIn ?: 0) else (qtyFromInput ?: 0),
                        scrap = 0,               // No hay scrap directo desde esta pantalla
                        errorCodeId = null,      // Sin código de error
                        comments = null          // Sin comentarios
                    )
                }
            }.onSuccess { response ->
                val responseSuccess = when (response) {
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

    fun prepareProductAdvanceRegistration(workOrderNumber: String, locationName: String) {
        if (!validateManualQty()) return

        val state = _uiState.value
        val decision = productAdvanceScanPolicy.evaluate(
            workOrderNumber = workOrderNumber,
            qtyInput = state.qtyInput,
            locationName = locationName,
            partInfo = state.partInfo,
            context = state.contextInfo
        )

        if (!decision.canRegister) {
            _uiState.update { it.copy(errorMessage = decision.localMessage, pendingReady = false) }
            return
        }

        val qtyFromInput = state.qtyInput.toIntOrNull() ?: 0
        val previousQty = state.contextInfo?.previousQuantity ?: 0
        val diff = previousQty - qtyFromInput

        _uiState.update {
            it.copy(
                errorMessage = null,
                pendingQtyIn = decision.qtyIn,
                piecesDifference = if (diff > 0) diff else 0,
                pendingReady = true
            )
        }
    }

    fun clearPendingRegistration() {
        _uiState.update { it.copy(pendingReady = false) }
    }
}

class TaskDetailViewModelFactory(
    private val scannerRepository: ScannerRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = TaskDetailViewModel(scannerRepository, authRepository) as T
}