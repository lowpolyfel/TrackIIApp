package com.ttelectronics.trackiiapp.data.network

import com.ttelectronics.trackiiapp.data.models.auth.LocationDto
import com.ttelectronics.trackiiapp.data.models.auth.LoginRequest
import com.ttelectronics.trackiiapp.data.models.auth.LoginResponse
import com.ttelectronics.trackiiapp.data.models.auth.RegisterRequest
import com.ttelectronics.trackiiapp.data.models.auth.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @GET("api/locations")
    suspend fun getLocations(): List<LocationDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}
