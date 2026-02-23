package com.ttelectronics.trackiiapp.data.local

import android.content.Context
import androidx.core.content.edit

class AppSession(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isLoggedIn: Boolean = false
        private set
    var userId: Int = 0
        private set
    var deviceId: Int = 0
        private set
    var locationId: Int = 0
        private set
    var username: String = "Sin usuario"
        private set
    var deviceName: String = "Dispositivo"
        private set
    var locationName: String = "Sin localidad"
        private set

    init {
        loadFromPreferences()
    }

    fun setLoggedIn(
        userId: Int,
        username: String,
        deviceId: Int,
        deviceName: String?,
        locationId: Int,
        locationName: String?
    ) {
        isLoggedIn = true
        this.userId = userId
        this.deviceId = deviceId
        this.locationId = locationId
        if (username.isNotBlank()) this.username = username
        if (!deviceName.isNullOrBlank()) this.deviceName = deviceName
        if (!locationName.isNullOrBlank()) this.locationName = locationName
        saveToPreferences()
    }

    fun clear() {
        isLoggedIn = false
        userId = 0
        deviceId = 0
        locationId = 0
        username = "Sin usuario"
        deviceName = "Dispositivo"
        locationName = "Sin localidad"
        prefs.edit { clear() }
    }

    private fun loadFromPreferences() {
        if (!prefs.getBoolean(LOGGED_IN_KEY, false)) return
        isLoggedIn = true
        userId = prefs.getInt(USER_ID_KEY, 0)
        deviceId = prefs.getInt(DEVICE_ID_KEY, 0)
        locationId = prefs.getInt(LOCATION_ID_KEY, 0)
        username = prefs.getString(USERNAME_KEY, username) ?: username
        deviceName = prefs.getString(DEVICE_NAME_KEY, deviceName) ?: deviceName
        locationName = prefs.getString(LOCATION_NAME_KEY, locationName) ?: locationName
    }

    private fun saveToPreferences() {
        prefs.edit {
            putBoolean(LOGGED_IN_KEY, isLoggedIn)
            putInt(USER_ID_KEY, userId)
            putInt(DEVICE_ID_KEY, deviceId)
            putInt(LOCATION_ID_KEY, locationId)
            putString(USERNAME_KEY, username)
            putString(DEVICE_NAME_KEY, deviceName)
            putString(LOCATION_NAME_KEY, locationName)
        }
    }

    companion object {
        private const val PREFS_NAME = "trackii.session"
        private const val LOGGED_IN_KEY = "trackii.session.logged_in"
        private const val USER_ID_KEY = "trackii.session.user_id"
        private const val DEVICE_ID_KEY = "trackii.session.device_id"
        private const val LOCATION_ID_KEY = "trackii.session.location_id"
        private const val USERNAME_KEY = "trackii.session.username"
        private const val DEVICE_NAME_KEY = "trackii.session.device_name"
        private const val LOCATION_NAME_KEY = "trackii.session.location_name"
    }
}
