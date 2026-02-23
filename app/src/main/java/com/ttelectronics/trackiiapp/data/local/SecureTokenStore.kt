package com.ttelectronics.trackiiapp.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureTokenStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(token: String) {
        prefs.edit().putString(ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN, null)

    fun clear() {
        prefs.edit().remove(ACCESS_TOKEN).apply()
    }

    companion object {
        private const val PREFS = "trackii.secure.storage"
        private const val ACCESS_TOKEN = "trackii.access.token"
    }
}
