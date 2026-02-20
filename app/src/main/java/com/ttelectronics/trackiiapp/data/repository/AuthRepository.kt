package com.ttelectronics.trackiiapp.data.repository

import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.local.SecureTokenStore
import com.ttelectronics.trackiiapp.data.models.auth.LocationDto
import com.ttelectronics.trackiiapp.data.models.auth.LoginRequest
import com.ttelectronics.trackiiapp.data.models.auth.LoginResponse
import com.ttelectronics.trackiiapp.data.models.auth.RegisterRequest
import com.ttelectronics.trackiiapp.data.models.auth.RegisterResponse
import com.ttelectronics.trackiiapp.data.network.AuthApiService

class AuthRepository(
    private val api: AuthApiService,
    private val tokenStore: SecureTokenStore,
    private val appSession: AppSession
) {
    suspend fun getLocations(): List<LocationDto> = api.getLocations().filter { it.active }

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

    fun logout() {
        tokenStore.clear()
        appSession.clear()
    }
}
