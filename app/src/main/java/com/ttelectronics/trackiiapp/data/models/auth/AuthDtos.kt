package com.ttelectronics.trackiiapp.data.models.auth

import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("active") val active: Boolean = true
)

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceUid") val deviceUid: String
)

data class LoginResponse(
    @SerializedName(value = "accessToken", alternate = ["token", "access_token"]) val accessToken: String = "",
    @SerializedName("userId") val userId: Int = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("deviceId") val deviceId: Int = 0,
    @SerializedName("deviceName") val deviceName: String? = null,
    @SerializedName("locationId") val locationId: Int = 0,
    @SerializedName("locationName") val locationName: String? = null
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("tokenCode") val tokenCode: String,
    @SerializedName("locationId") val locationId: Int,
    @SerializedName("deviceUid") val deviceUid: String,
    @SerializedName("deviceName") val deviceName: String
)

data class RegisterResponse(
    @SerializedName("userId") val userId: Int = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("message") val message: String = "Registro completado"
)

data class TokenValidationResponse(
    @SerializedName("valid") val valid: Boolean? = null,
    @SerializedName("isValid") val isValid: Boolean? = null
)
