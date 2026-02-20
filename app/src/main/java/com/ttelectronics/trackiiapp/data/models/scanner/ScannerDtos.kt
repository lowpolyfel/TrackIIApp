package com.ttelectronics.trackiiapp.data.models.scanner

import com.google.gson.annotations.SerializedName

data class PartLookupResponse(
    @SerializedName(value = "found", alternate = ["exists", "isFound", "Found"]) val found: Boolean? = null,
    @SerializedName(value = "partNumber", alternate = ["part_number", "PartNumber"]) val partNumber: String? = null,
    @SerializedName(value = "area", alternate = ["Area", "areaName", "AreaName"]) val area: String? = null,
    @SerializedName(value = "family", alternate = ["Family", "familyName", "FamilyName"]) val family: String? = null,
    @SerializedName(value = "subfamily", alternate = ["Subfamily", "subfamilyName", "SubfamilyName"]) val subfamily: String? = null,
    @SerializedName(value = "routeNumber", alternate = ["route_number", "routeId", "RouteNumber", "RouteId"]) val routeNumber: String? = null,
    @SerializedName(value = "currentRoute", alternate = ["currentStep", "currentLocation", "CurrentRoute", "CurrentStep"]) val currentRoute: String? = null
)
