package com.ttelectronics.trackiiapp.data.network

import com.google.gson.GsonBuilder
import com.ttelectronics.trackiiapp.core.config.AppConfig
import com.ttelectronics.trackiiapp.data.local.SecureTokenStore
import com.ttelectronics.trackiiapp.data.network.ScannerApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient(tokenStore: SecureTokenStore) {
    private val authInterceptor = Interceptor { chain ->
        val token = tokenStore.getAccessToken()
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().serializeNulls().create()
            )
        )
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val scannerApiService: ScannerApiService = retrofit.create(ScannerApiService::class.java)
}
