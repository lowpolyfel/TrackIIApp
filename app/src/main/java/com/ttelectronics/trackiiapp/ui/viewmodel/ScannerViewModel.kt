package com.ttelectronics.trackiiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val isValidating: Boolean = false,
    val validationError: Int? = null,
    val isProductFound: Boolean = false,
    val shouldNavigate: Boolean = false,
    val isLotFound: Boolean = false,
    val isPartFound: Boolean = false,
    val scannedLot: String = "",
    val scannedPart: String = ""
)

class ScannerViewModel(private val scannerRepository: ScannerRepository) : ViewModel() {
    private val lotRegex = Regex("^[0-9]{7}$")
    private val partRegex = Regex("^[A-Z](?=.*[0-9])[A-Z0-9._/-]{3,}$")

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var lotScanState = StableScanState()
    private var partScanState = StableScanState()

    fun procesarFotograma(codigosCrudos: List<String>) {
        viewModelScope.launch(Dispatchers.Default) {
            val normalizados = codigosCrudos.mapNotNull { raw ->
                raw.trim().uppercase().replace(" ", "").takeIf { it.isNotEmpty() }
            }

            if (normalizados.isEmpty()) {
                lotScanState = lotScanState.clear()
                partScanState = partScanState.clear()
                return@launch
            }

            val now = System.currentTimeMillis()
            val lotCandidate = normalizados.firstOrNull { lotRegex.matches(it) }
            val partCandidate = normalizados.firstOrNull { partRegex.matches(it) }

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
                shouldNavigate = false
            )
        }
    }

    fun consumeScanEffects() {
        _uiState.update { it.copy(isLotFound = false, isPartFound = false) }
    }

    fun validatePart(partNumber: String) {
        if (partNumber.isBlank()) {
            _uiState.update { it.copy(validationError = R.string.error_part_mandatory) }
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
                            validationError = if (found) null else R.string.error_order_not_found_for_part
                        )
                    }
                }
                .onFailure { _ ->
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

    fun consumeNavigation() {
        _uiState.update { it.copy(shouldNavigate = false) }
    }

    private fun requiredStableReads(): Int = 2
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
