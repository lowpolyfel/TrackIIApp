package com.ttelectronics.trackiiapp.core.demo

import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCategoryResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCodeResponse
import com.ttelectronics.trackiiapp.data.models.scanner.NextRouteStepResponse
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.TimelineStepResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DemoScanScenario(val buttonLabel: String) {
    OutOfRoute("Fuera de ruta"),
    Success("Pasa normal"),
    NotRegistered("No registrado")
}

object DemoMode {
    const val expectedPieces: Int = 7200

    private val selectedScenarioFlow = MutableStateFlow<DemoScanScenario?>(null)
    val selectedScenario = selectedScenarioFlow.asStateFlow()

    fun activateProductAdvanceScenario(scenario: DemoScanScenario) {
        selectedScenarioFlow.value = scenario
    }

    fun activeProductAdvanceScenario(): DemoScanScenario? = selectedScenarioFlow.value

    fun isProductAdvanceDemoEnabled(): Boolean = selectedScenarioFlow.value != null

    fun demoPartInfo(partNumber: String): PartLookupResponse = PartLookupResponse(
        found = true,
        message = "Modo demo activo.",
        partNumber = partNumber.ifBlank { "DEMO-PART-001" },
        productId = 1,
        subfamilyId = 10,
        subfamilyName = "Subfamilia Demo",
        familyId = 20,
        familyName = "Familia Demo",
        areaId = 30,
        areaName = "Producción Demo",
        activeRouteId = 40
    )

    fun demoContext(locationName: String): WorkOrderContextResponse = WorkOrderContextResponse(
        isNew = false,
        orderStatus = "Active",
        wipStatus = "Active",
        statusUpdatedAt = "2026-03-23 00:00:00",
        previousQuantity = expectedPieces,
        currentStepNumber = 2,
        currentStepName = locationName.ifBlank { "Localidad Demo" },
        routeName = "Ruta Demo",
        nextSteps = listOf(
            NextRouteStepResponse(
                stepId = 3,
                stepNumber = 3,
                stepName = "Empaque",
                locationId = 300,
                locationName = "Empaque Demo"
            )
        ),
        timeline = listOf(
            TimelineStepResponse(
                stepOrder = 1,
                locationName = "Entrada Demo",
                state = "DONE",
                pieces = expectedPieces.toString(),
                scrap = "0"
            ),
            TimelineStepResponse(
                stepOrder = 2,
                locationName = locationName.ifBlank { "Localidad Demo" },
                state = "CURRENT",
                pieces = expectedPieces.toString(),
                scrap = "0"
            )
        )
    )

    fun demoErrorCategories(): List<ErrorCategoryResponse> = listOf(
        ErrorCategoryResponse(id = 1, name = "Calidad"),
        ErrorCategoryResponse(id = 2, name = "Material"),
        ErrorCategoryResponse(id = 3, name = "Proceso")
    )

    fun demoErrorCodes(categoryId: Int): List<ErrorCodeResponse> = when (categoryId) {
        1 -> listOf(
            ErrorCodeResponse(id = 101, code = "Q-01", description = "Daño cosmético"),
            ErrorCodeResponse(id = 102, code = "Q-02", description = "Falla funcional")
        )
        2 -> listOf(
            ErrorCodeResponse(id = 201, code = "M-01", description = "Material incorrecto"),
            ErrorCodeResponse(id = 202, code = "M-02", description = "Componente faltante")
        )
        else -> listOf(
            ErrorCodeResponse(id = 301, code = "P-01", description = "Ajuste de proceso"),
            ErrorCodeResponse(id = 302, code = "P-02", description = "Error de set-up")
        )
    }

    fun defaultDemoErrorCode(): ErrorCodeResponse = demoErrorCodes(1).first()
}
