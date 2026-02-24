package com.ttelectronics.trackiiapp.data.models.scanner

import com.google.gson.annotations.SerializedName

data class PartLookupResponse(
    @SerializedName(value = "found", alternate = ["exists", "isFound", "Found"]) val found: Boolean? = null,
    @SerializedName(value = "partNumber", alternate = ["part_number", "PartNumber"]) val partNumber: String? = null,
    @SerializedName(value = "area", alternate = ["Area", "areaName", "AreaName"]) val area: String? = null,
    @SerializedName(value = "areaId", alternate = ["AreaId"]) val areaId: Int? = null,
    @SerializedName(value = "family", alternate = ["Family", "familyName", "FamilyName"]) val family: String? = null,
    @SerializedName(value = "subfamily", alternate = ["Subfamily", "subfamilyName", "SubfamilyName"]) val subfamily: String? = null,
    @SerializedName(value = "routeNumber", alternate = ["route_number", "routeId", "RouteNumber", "RouteId"]) val routeNumber: String? = null,
    @SerializedName(value = "currentRoute", alternate = ["currentStep", "currentLocation", "CurrentRoute", "CurrentStep"]) val currentRoute: String? = null
)

// DTO para recibir el contexto antes de registrar
data class WorkOrderContextResponse(
    @SerializedName("isNew") val isNew: Boolean,
    @SerializedName("previousQuantity") val previousQuantity: Int?,
    @SerializedName("currentStepNumber") val currentStepNumber: Int,
    @SerializedName("currentStepName") val currentStepName: String,
    @SerializedName("nextSteps") val nextSteps: List<String>
)

// DTO para enviar el registro
data class RegisterScanRequest(
    @SerializedName("workOrderNumber") val workOrderNumber: String,
    @SerializedName("partNumber") val partNumber: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("isAlloyTablet") val isAlloyTablet: Boolean
)

// DTO para la respuesta del registro
data class RegisterScanResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("routeFinished") val routeFinished: Boolean
)

data class ScrapRequest(
    @SerializedName("workOrderNumber") val workOrderNumber: String,
    @SerializedName("partNumber") val partNumber: String,
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("qty") val qty: Int,
    @SerializedName("reason") val reason: String
)

data class ScrapResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

data class ReworkRequest(
    @SerializedName("workOrderNumber") val workOrderNumber: String,
    @SerializedName("partNumber") val partNumber: String,
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("location") val location: String,
    @SerializedName("reason") val reason: String? = null
)

data class ReworkResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)
