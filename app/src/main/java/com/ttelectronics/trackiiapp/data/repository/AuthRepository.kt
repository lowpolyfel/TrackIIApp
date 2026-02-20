package com.ttelectronics.trackiiapp.data.repository

import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.local.SecureTokenStore
import com.ttelectronics.trackiiapp.data.models.auth.LocationDto
import com.ttelectronics.trackiiapp.data.models.auth.LoginRequest
import com.ttelectronics.trackiiapp.data.models.auth.LoginResponse
import com.ttelectronics.trackiiapp.data.models.auth.RegisterRequest
import com.ttelectronics.trackiiapp.data.models.auth.RegisterResponse
import com.ttelectronics.trackiiapp.data.network.AuthApiService

data class SessionSnapshot(
    val isLoggedIn: Boolean,
    val username: String,
    val locationName: String,
    val deviceName: String,
    val userId: Int,
    val locationId: Int,
    val deviceId: Int
)

class AuthRepository(
    private val api: AuthApiService,
    private val tokenStore: SecureTokenStore,
    private val appSession: AppSession
) {
    suspend fun getLocations(): List<LocationDto> = api.getLocations().filter { it.active }

    suspend fun validateToken(tokenCode: String): Boolean {
        val normalized = tokenCode.trim()
        if (normalized.isBlank()) return false

        val queryAttempt = runCatching { api.validateTokenQuery(normalized) }.getOrNull()
        if (queryAttempt?.isSuccessful == true) {
            val body = queryAttempt.body()
            return body?.valid ?: body?.isValid ?: true
        }

        val pathAttempt = runCatching { api.validateTokenPath(normalized) }.getOrNull()
        if (pathAttempt?.isSuccessful == true) {
            val body = pathAttempt.body()
            return body?.valid ?: body?.isValid ?: true
        }

        return false
    }

    suspend fun login(username: String, password: String): LoginResponse {
        val payload = api.login(LoginRequest(username = username, password = password))
        if (payload.accessToken.isNotBlank()) tokenStore.saveAccessToken(payload.accessToken)
        appSession.setLoggedIn(
            userId = payload.userId,
            username = payload.username,
            deviceId = payload.deviceId,
            deviceName = payload.deviceName,
            locationId = payload.locationId,
            locationName = payload.locationName
        )
        return payload
    }

    suspend fun register(request: RegisterRequest): RegisterResponse = api.register(request)

    fun sessionSnapshot(): SessionSnapshot = SessionSnapshot(
        isLoggedIn = appSession.isLoggedIn,
        username = appSession.username,
        locationName = appSession.locationName,
        deviceName = appSession.deviceName,
        userId = appSession.userId,
        locationId = appSession.locationId,
        deviceId = appSession.deviceId
    )

    fun logout() {
        tokenStore.clear()
        appSession.clear()
    }
}
