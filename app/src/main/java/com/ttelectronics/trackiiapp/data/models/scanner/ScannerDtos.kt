package com.ttelectronics.trackiiapp.data.models.scanner

import com.google.gson.annotations.SerializedName

data class PartLookupResponse(
    @SerializedName(value = "found", alternate = ["exists", "isFound"]) val found: Boolean? = null,
    @SerializedName(value = "partNumber", alternate = ["part_number"]) val partNumber: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("family") val family: String? = null,
    @SerializedName("subfamily") val subfamily: String? = null,
    @SerializedName(value = "routeNumber", alternate = ["route_number", "routeId"]) val routeNumber: String? = null,
    @SerializedName(value = "currentRoute", alternate = ["currentStep", "currentLocation"]) val currentRoute: String? = null
)
