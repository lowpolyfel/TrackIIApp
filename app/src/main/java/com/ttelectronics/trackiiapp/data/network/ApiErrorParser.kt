package com.ttelectronics.trackiiapp.data.network

import org.json.JSONObject
import retrofit2.HttpException

object ApiErrorParser {
    fun readableError(exception: Throwable): String {
        val httpException = exception as? HttpException ?: return exception.message ?: "Error desconocido"
        val body = httpException.response()?.errorBody()?.string().orEmpty()
        if (body.isBlank()) return "Error ${httpException.code()}"
        return runCatching {
            val root = JSONObject(body)
            root.optString("detail")
                .ifBlank { root.optString("title") }
                .ifBlank { root.optString("message") }
                .ifBlank { body }
        }.getOrDefault(body)
    }
}
