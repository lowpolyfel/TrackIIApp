package com.ttelectronics.trackiiapp.domain.scanner

import com.ttelectronics.trackiiapp.data.models.enums.ScanType
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse

data class ProductAdvanceDecision(
    val scanType: ScanType,
    val qtyIn: Int,
    val localMessage: String? = null
)

class ProductAdvanceScanPolicy {

    fun evaluate(
        workOrderNumber: String,
        qtyInput: String,
        deviceName: String,
        partInfo: PartLookupResponse?,
        context: WorkOrderContextResponse?
    ): ProductAdvanceDecision {
        val qty = qtyInput.toIntOrNull() ?: 0
        if (qty <= 0) {
            return ProductAdvanceDecision(
                scanType = ScanType.ERROR,
                qtyIn = 0,
                localMessage = "Cantidad inválida. Se registrará como ERROR."
            )
        }

        val normalizedWo = workOrderNumber.trim()
        if (context?.workOrderId == null && normalizedWo.length != 7) {
            return ProductAdvanceDecision(
                scanType = ScanType.ERROR,
                qtyIn = qty,
                localMessage = "WO inválida (debe ser de 7 dígitos). Se registrará como ERROR."
            )
        }

        val isAlloyDevice = deviceName.contains("alloy", ignoreCase = true)
        if (context?.workOrderId == null && !isAlloyDevice) {
            return ProductAdvanceDecision(
                scanType = ScanType.ERROR,
                qtyIn = qty,
                localMessage = "Solo localidad ALLOY puede crear WO nueva. Se registrará como ERROR."
            )
        }

        if (partInfo?.areaId == 1 && (context?.isFirstStep == true) && !isAlloyDevice) {
            return ProductAdvanceDecision(
                scanType = ScanType.ERROR,
                qtyIn = qty,
                localMessage = "Solo tabletas Alloy pueden abrir ordenes de Discretos. Se registrará como ERROR."
            )
        }

        if (context?.canProceed == false) {
            return ProductAdvanceDecision(
                scanType = ScanType.ERROR,
                qtyIn = qty,
                localMessage = context.message ?: "El paso actual no corresponde con la ruta. Se registrará como ERROR."
            )
        }

        return ProductAdvanceDecision(
            scanType = ScanType.ENTRY,
            qtyIn = qty
        )
    }
}
