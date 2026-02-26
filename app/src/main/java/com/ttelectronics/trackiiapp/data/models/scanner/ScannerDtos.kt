package com.ttelectronics.trackiiapp.data.models.scanner

import com.google.gson.annotations.SerializedName
import com.ttelectronics.trackiiapp.data.models.enums.WipStatus
import com.ttelectronics.trackiiapp.data.models.enums.WorkOrderStatus

data class PartLookupResponse(
    @SerializedName(value = "found", alternate = ["exists", "isFound", "Found"]) val found: Boolean? = null,
    @SerializedName(value = "partNumber", alternate = ["part_number", "PartNumber"]) val partNumber: String? = null,
    @SerializedName(value = "area", alternate = ["Area", "areaName", "AreaName"]) val area: String? = null,
    @SerializedName(value = "areaId", alternate = ["AreaId"]) val areaId: Int? = null,
    @SerializedName(value = "family", alternate = ["Family", "familyName", "FamilyName"]) val family: String? = null,
    @SerializedName(value = "subfamily", alternate = ["Subfamily", "subfamilyName", "SubfamilyName"]) val subfamily: String? = null,
    @SerializedName(value = "routeNumber", alternate = ["route_number", "routeId", "RouteNumber", "RouteId"]) val routeNumber: String? = null,
    @SerializedName(value = "currentRoute", alternate = ["currentStep", "currentLocation", "CurrentRoute", "CurrentStep"]) val currentRoute: String? = null,
    @SerializedName(value = "currentLocationName", alternate = ["CurrentLocationName"]) val currentLocationName: String? = null,
    @SerializedName(value = "nextLocationName", alternate = ["NextLocationName"]) val nextLocationName: String? = null
)

data class WorkOrderContextResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("workOrderId") val workOrderId: Int? = null,
    @SerializedName("status") val status: WorkOrderStatus? = null,
    @SerializedName("routeId") val routeId: Int? = null,
    @SerializedName("currentStepId") val currentStepId: Int? = null,
    @SerializedName("nextStepId") val nextStepId: Int? = null,
    @SerializedName("currentLocationName") val currentLocationName: String? = null,
    @SerializedName("nextLocationName") val nextLocationName: String? = null,
    @SerializedName("routeVersion") val routeVersion: String? = null,
    @SerializedName("routeName") val routeName: String? = null,
    @SerializedName("isFirstStep") val isFirstStep: Boolean? = null,
    @SerializedName("canProceed") val canProceed: Boolean? = null,
    @SerializedName("wipStatus") val wipStatus: WipStatus? = null
)

data class RegisterScanRequest(
    @SerializedName("WorkOrderNumber") val workOrderNumber: String,
    @SerializedName("PartNumber") val partNumber: String,
    @SerializedName("Quantity") val quantity: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("DeviceId") val deviceId: Int
)

data class RegisterScanResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
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
