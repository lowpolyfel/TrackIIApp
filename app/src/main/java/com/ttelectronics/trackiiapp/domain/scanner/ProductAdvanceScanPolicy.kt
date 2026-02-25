package com.ttelectronics.trackiiapp.domain.scanner

import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse

data class ProductAdvanceDecision(
    val canRegister: Boolean,
    val qtyIn: Int,
    val localMessage: String? = null
)

class ProductAdvanceScanPolicy {

    fun evaluate(
        workOrderNumber: String,
        qtyInput: String,
        locationName: String,
        partInfo: PartLookupResponse?,
        context: WorkOrderContextResponse?
    ): ProductAdvanceDecision {
        val qty = qtyInput.toIntOrNull() ?: 0
        if (qty <= 0) {
            return ProductAdvanceDecision(
                canRegister = false,
                qtyIn = 0,
                localMessage = "Cantidad inválida."
            )
        }

        val normalizedWo = workOrderNumber.trim()
        if (context?.workOrderId == null && normalizedWo.length != 7) {
            return ProductAdvanceDecision(
                canRegister = false,
                qtyIn = qty,
                localMessage = "WO inválida (debe ser de 7 dígitos)."
            )
        }



        if (context?.canProceed == false) {
            return ProductAdvanceDecision(
                canRegister = false,
                qtyIn = qty,
                localMessage = context.message ?: "Dispositivo no corresponde al paso actual."
            )
        }

        return ProductAdvanceDecision(
            canRegister = true,
            qtyIn = qty
        )
    }
}
