package com.ttelectronics.trackiiapp.data.models.auth

import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("id") val id: UInt = 0u,
    @SerializedName("name") val name: String = "",
    @SerializedName("active") val active: Boolean = true
)

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String = "",
    @SerializedName("userId") val userId: UInt = 0u,
    @SerializedName("username") val username: String = "",
    @SerializedName("deviceId") val deviceId: UInt = 0u,
    @SerializedName("deviceName") val deviceName: String? = null,
    @SerializedName("locationId") val locationId: UInt = 0u,
    @SerializedName("locationName") val locationName: String? = null
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("tokenCode") val tokenCode: String,
    @SerializedName("locationId") val locationId: UInt,
    @SerializedName("deviceUid") val deviceUid: String,
    @SerializedName("deviceName") val deviceName: String
)

data class RegisterResponse(
    @SerializedName("userId") val userId: UInt = 0u,
    @SerializedName("username") val username: String = "",
    @SerializedName("message") val message: String = "Registro completado"
)
