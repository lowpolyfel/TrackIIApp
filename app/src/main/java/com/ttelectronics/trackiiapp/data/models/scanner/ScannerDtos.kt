package com.ttelectronics.trackiiapp.data.models.scanner

import com.google.gson.annotations.SerializedName

data class PartLookupResponse(
    @SerializedName("found") val found: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("partNumber") val partNumber: String? = null,
    @SerializedName("productId") val productId: Int? = null,
    @SerializedName("subfamilyId") val subfamilyId: Int? = null,
    @SerializedName("subfamilyName") val subfamilyName: String? = null,
    @SerializedName("familyId") val familyId: Int? = null,
    @SerializedName("familyName") val familyName: String? = null,
    @SerializedName("areaId") val areaId: Int? = null,
    @SerializedName("areaName") val areaName: String? = null,
    @SerializedName("activeRouteId") val activeRouteId: Int? = null
)

data class WorkOrderContextResponse(
    @SerializedName("isNew") val isNew: Boolean? = null,
    @SerializedName("previousQuantity") val previousQuantity: Int? = null,
    @SerializedName("currentStepNumber") val currentStepNumber: Int? = null,
    @SerializedName("currentStepName") val currentStepName: String? = null,
    @SerializedName("routeName") val routeName: String? = null,
    @SerializedName("nextSteps") val nextSteps: List<NextRouteStepResponse>? = null
)

data class NextRouteStepResponse(
    @SerializedName("stepId") val stepId: Int,
    @SerializedName("stepNumber") val stepNumber: Int,
    @SerializedName("stepName") val stepName: String,
    @SerializedName("locationId") val locationId: Int,
    @SerializedName("locationName") val locationName: String
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
