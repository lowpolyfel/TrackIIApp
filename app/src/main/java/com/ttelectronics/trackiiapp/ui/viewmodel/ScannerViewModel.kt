package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.data.models.enums.WipStatus
import com.ttelectronics.trackiiapp.data.network.ApiErrorParser
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val isValidating: Boolean = false,
    val validationError: Int? = null,
    val customValidationMessage: String? = null,
    val isProductFound: Boolean = false,
    val shouldNavigate: Boolean = false,
    val navigationTarget: ScannerNavigationTarget = ScannerNavigationTarget.ScanReview,
    val isLotFound: Boolean = false,
    val isPartFound: Boolean = false,
    val scannedLot: String = "",
    val scannedPart: String = ""
)

enum class ScannerNavigationTarget {
    ScanReview,
    ReworkTask,
    ReworkRelease
}

class ScannerViewModel(private val scannerRepository: ScannerRepository) : ViewModel() {
    private val lotRegex = Regex("^[0-9]{7}$")
    private val partRegex = Regex("^[A-Z0-9\\-]{6,25}$")

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var lotScanState = StableScanState()
    private var partScanState = StableScanState()

    fun procesarFotograma(codigosCrudos: List<String>) {
        viewModelScope.launch(Dispatchers.Default) {
            val normalizados = codigosCrudos.mapNotNull(::normalizeBarcode).filter { it.isNotEmpty() }

            if (normalizados.isEmpty()) {
                lotScanState = lotScanState.clear()
                partScanState = partScanState.clear()
                return@launch
            }

            val now = System.currentTimeMillis()
            val lotCandidate = normalizados.firstOrNull { lotRegex.matches(it) }
            val partCandidate = normalizados.firstOrNull { candidate ->
                !lotRegex.matches(candidate) && partRegex.matches(candidate)
            }

            var lotFound = false
            var partFound = false
            var scannedLot = ""
            var scannedPart = ""

            if (_uiState.value.scannedLot.isBlank() && lotCandidate != null) {
                lotScanState = lotScanState.record(lotCandidate)
                if (lotScanState.canAccept(now, requiredStableReads())) {
                    lotScanState = lotScanState.markAccepted(now)
                    scannedLot = lotCandidate
                    lotFound = true
                }
            }

            if (_uiState.value.scannedPart.isBlank() && partCandidate != null) {
                partScanState = partScanState.record(partCandidate)
                if (partScanState.canAccept(now, requiredStableReads())) {
                    partScanState = partScanState.markAccepted(now)
                    scannedPart = partCandidate
                    partFound = true
                }
            }

            if (lotFound || partFound) {
                _uiState.update {
                    it.copy(
                        scannedLot = if (lotFound) scannedLot else it.scannedLot,
                        scannedPart = if (partFound) scannedPart else it.scannedPart,
                        isLotFound = lotFound,
                        isPartFound = partFound
                    )
                }
            }
        }
    }

    fun resetScan() {
        lotScanState = StableScanState()
        partScanState = StableScanState()
        _uiState.update {
            it.copy(
                isLotFound = false,
                isPartFound = false,
                scannedLot = "",
                scannedPart = "",
                isValidating = false,
                validationError = null,
                customValidationMessage = null,
                shouldNavigate = false,
                navigationTarget = ScannerNavigationTarget.ScanReview
            )
        }
    }

    fun consumeScanEffects() {
        _uiState.update { it.copy(isLotFound = false, isPartFound = false) }
    }

    fun validateForTask(taskType: TaskType, lotNumber: String, partNumber: String) {
        val normalizedPartNumber = normalizeBarcode(partNumber)
        val normalizedLot = normalizeBarcode(lotNumber)

        if (normalizedPartNumber.isBlank()) {
            _uiState.update { it.copy(validationError = R.string.error_part_mandatory) }
            return
        }

        if (taskType == TaskType.Rework) {
            validateReworkOrder(normalizedLot)
            return
        }

        validatePartExists(normalizedPartNumber)
    }

    private fun validatePartExists(partNumber: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isValidating = true,
                    validationError = null,
                    customValidationMessage = null,
                    shouldNavigate = false,
                    navigationTarget = ScannerNavigationTarget.ScanReview
                )
            }
            runCatching { scannerRepository.validatePartExists(partNumber) }
                .onSuccess { found ->
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            isProductFound = found,
                            shouldNavigate = true,
                            navigationTarget = ScannerNavigationTarget.ScanReview,
                            validationError = if (found) null else R.string.error_order_not_found_for_part
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            shouldNavigate = false,
                            validationError = R.string.error_generic_validation
                        )
                    }
                }
        }
    }

    private fun validateReworkOrder(workOrderNumber: String) {
        if (workOrderNumber.isBlank()) {
            _uiState.update { it.copy(validationError = R.string.error_part_mandatory) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isValidating = true,
                    validationError = null,
                    customValidationMessage = null,
                    shouldNavigate = false
                )
            }
            runCatching { scannerRepository.validateRework(workOrderNumber) }
                .onSuccess { response ->
                    when (parseWipStatus(response.status)) {
                        WipStatus.ACTIVE -> {
                            _uiState.update {
                                it.copy(
                                    isValidating = false,
                                    isProductFound = true,
                                    shouldNavigate = true,
                                    navigationTarget = ScannerNavigationTarget.ReworkTask
                                )
                            }
                        }

                        WipStatus.HOLD -> {
                            _uiState.update {
                                it.copy(
                                    isValidating = false,
                                    isProductFound = true,
                                    shouldNavigate = true,
                                    navigationTarget = ScannerNavigationTarget.ReworkRelease
                                )
                            }
                        }

                        WipStatus.FINISHED, WipStatus.SCRAPPED -> {
                            _uiState.update {
                                it.copy(
                                    isValidating = false,
                                    shouldNavigate = true,
                                    navigationTarget = ScannerNavigationTarget.ScanReview,
                                    customValidationMessage = "No se puede retrabajar una orden terminada o cancelada."
                                )
                            }
                        }

                        else -> {
                            _uiState.update {
                                it.copy(
                                    isValidating = false,
                                    shouldNavigate = true,
                                    navigationTarget = ScannerNavigationTarget.ScanReview,
                                    customValidationMessage = response.message ?: "No fue posible validar el estado del lote."
                                )
                            }
                        }
                    }
                }
                .onFailure { ex ->
                    val errorMessage = ApiErrorParser.readableError(ex)
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            shouldNavigate = true,
                            navigationTarget = ScannerNavigationTarget.ScanReview,
                            isProductFound = false,
                            validationError = null,
                            customValidationMessage = if (errorMessage.contains("no empieza", ignoreCase = true)) {
                                "Esta orden aún no empieza."
                            } else {
                                "No se pudo validar la orden."
                            }
                        )
                    }
                }
        }
    }

    private fun parseWipStatus(rawValue: String?): WipStatus {
        val normalized = rawValue?.trim()?.uppercase() ?: return WipStatus.ERROR
        return when (normalized) {
            "ACTIVE" -> WipStatus.ACTIVE
            "HOLD" -> WipStatus.HOLD
            "FINISHED" -> WipStatus.FINISHED
            "SCRAPPED" -> WipStatus.SCRAPPED
            else -> WipStatus.ERROR
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(shouldNavigate = false) }
    }

    private fun normalizeBarcode(value: String): String =
        value.trim().uppercase().replace("\\s+".toRegex(), "")

    private fun requiredStableReads(): Int = 4
}

private data class StableScanState(val candidate: String = "", val count: Int = 0, val lastAcceptedAt: Long = 0L) {
    fun record(value: String): StableScanState =
        if (candidate == value) copy(count = count + 1) else StableScanState(candidate = value, count = 1, lastAcceptedAt = lastAcceptedAt)

    fun canAccept(now: Long, requiredReads: Int): Boolean = count >= requiredReads && now - lastAcceptedAt > 800L
    fun markAccepted(now: Long): StableScanState = copy(lastAcceptedAt = now)
    fun clear(): StableScanState = if (candidate.isEmpty() && count == 0) this else StableScanState(lastAcceptedAt = lastAcceptedAt)
}

class ScannerViewModelFactory(private val scannerRepository: ScannerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ScannerViewModel(scannerRepository) as T
}
