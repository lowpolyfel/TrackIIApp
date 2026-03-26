package com.ttelectronics.trackiiapp.core

import android.content.Context
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.data.local.SecureTokenStore
import com.ttelectronics.trackiiapp.data.network.ApiClient
import com.ttelectronics.trackiiapp.data.repository.AuthRepository
import com.ttelectronics.trackiiapp.data.repository.ScannerRepository
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object ServiceLocator {
    @Volatile private var authRepository: AuthRepository? = null
    @Volatile private var scannerRepository: ScannerRepository? = null

    fun authRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: buildAuthRepository(context.applicationContext).also { authRepository = it }
        }
    }

    fun scannerRepository(context: Context): ScannerRepository {
        return scannerRepository ?: synchronized(this) {
            scannerRepository ?: buildScannerRepository(context.applicationContext).also { scannerRepository = it }
        }
    }

    private fun buildAuthRepository(context: Context): AuthRepository {
        val tokenStore = SecureTokenStore(context)
        val appSession = AppSession(context)
        val client = ApiClient(tokenStore)
        return AuthRepository(client.authApiService, tokenStore, appSession)
    }

    private fun buildScannerRepository(context: Context): ScannerRepository {
        val tokenStore = SecureTokenStore(context)
        val appSession = AppSession(context)
        val client = ApiClient(tokenStore)
        return ScannerRepository(client.scannerApiService, appSession)
    }
}
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}