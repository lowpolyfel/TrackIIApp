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

data class WorkOrderContextResponse(
    @SerializedName(value = "workOrderNumber", alternate = ["woNumber", "workOrder"]) val workOrderNumber: String? = null,
    @SerializedName(value = "routeNumber", alternate = ["routeId", "route"]) val routeNumber: String? = null,
    @SerializedName(value = "currentRoute", alternate = ["currentStep", "currentLocation"]) val currentRoute: String? = null,
    @SerializedName(value = "nextRoute", alternate = ["nextStep", "nextLocation"]) val nextRoute: String? = null,
    @SerializedName(value = "expectedFirstStep", alternate = ["firstStep", "step1"]) val expectedFirstStep: String? = null,
    @SerializedName(value = "hasWipItem", alternate = ["started", "inProgress"]) val hasWipItem: Boolean? = null,
    @SerializedName(value = "previousQty", alternate = ["prevQty", "qtyPrevious"]) val previousQty: Int? = null
)

data class RegisterScanRequest(
    @SerializedName("workOrderNumber") val workOrderNumber: String,
    @SerializedName("partNumber") val partNumber: String,
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("scanType") val scanType: String = "ENTRY",
    @SerializedName("qtyIn") val qtyIn: Int? = null
)

data class RegisterScanResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)
