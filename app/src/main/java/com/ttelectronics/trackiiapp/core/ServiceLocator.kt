package com.ttelectronics.trackiiapp.core

import android.content.Context
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.local.SecureTokenStore
import com.ttelectronics.trackiiapp.data.network.ApiClient
import com.ttelectronics.trackiiapp.data.repository.AuthRepository

object ServiceLocator {
    @Volatile private var authRepository: AuthRepository? = null

    fun authRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: buildAuthRepository(context.applicationContext).also { authRepository = it }
        }
    }

    private fun buildAuthRepository(context: Context): AuthRepository {
        val tokenStore = SecureTokenStore(context)
        val appSession = AppSession(context)
        val client = ApiClient(tokenStore)
        return AuthRepository(client.authApiService, tokenStore, appSession)
    }
}
