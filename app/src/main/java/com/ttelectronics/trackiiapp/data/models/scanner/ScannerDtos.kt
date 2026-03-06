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
    @SerializedName("orderStatus") val orderStatus: String? = null,
    @SerializedName("wipStatus") val wipStatus: String? = null,
    @SerializedName("statusUpdatedAt") val statusUpdatedAt: String? = null,
    @SerializedName("previousQuantity") val previousQuantity: Int? = null,
    @SerializedName("currentStepNumber") val currentStepNumber: Int? = null,
    @SerializedName("currentStepName") val currentStepName: String? = null,
    @SerializedName("routeName") val routeName: String? = null,
    @SerializedName("nextSteps") val nextSteps: List<NextRouteStepResponse>? = null,
    @SerializedName("timeline") val timeline: List<TimelineStepResponse>? = null
)

data class NextRouteStepResponse(
    @SerializedName("stepId") val stepId: Int,
    @SerializedName("stepNumber") val stepNumber: Int,
    @SerializedName("stepName") val stepName: String,
    @SerializedName("locationId") val locationId: Int,
    @SerializedName("locationName") val locationName: String
)

data class TimelineStepResponse(
    @SerializedName("stepOrder") val stepOrder: Int,
    @SerializedName("locationName") val locationName: String,
    @SerializedName("state") val state: String,
    @SerializedName("pieces") val pieces: String,
    @SerializedName("scrap") val scrap: String,
    @SerializedName("errorCode") val errorCode: String? = null, // NUEVO
    @SerializedName("comments") val comments: String? = null    // NUEVO
)
data class RegisterScanRequest(
    @SerializedName("WorkOrderNumber") val workOrderNumber: String,
    @SerializedName("PartNumber") val partNumber: String,
    @SerializedName("Quantity") val quantity: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("DeviceId") val deviceId: Int
)

data class RegisterScanResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("workOrderId") val workOrderId: Int? = null,
    @SerializedName("wipItemId") val wipItemId: Int? = null,
    @SerializedName("routeStepId") val routeStepId: Int? = null,
    @SerializedName("isFinalStep") val isFinalStep: Boolean? = null
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

data class ErrorCategoryResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class ErrorCodeResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String
)

data class ScrapOrderRequest(
    @SerializedName("workOrderNumber") val workOrderNumber: String,
    @SerializedName("partNumber") val partNumber: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("errorCodeId") val errorCodeId: Int,
    @SerializedName("comments") val comments: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("deviceId") val deviceId: Int
)

data class ReworkRequest(
    @SerializedName("workOrderNumber") val workOrderNumber: String,
    @SerializedName("partNumber") val partNumber: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("locationId") val locationId: Int,
    @SerializedName("isRelease") val isRelease: Boolean,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("userId") val userId: Int,
    @SerializedName("deviceId") val deviceId: Int
)

data class ReworkResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

data class ReworkValidationResponse(
    @SerializedName("exists") val exists: Boolean? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("workOrderNumber") val workOrderNumber: String? = null,
    @SerializedName("message") val message: String? = null
)


data class ReleaseWipItemResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)
