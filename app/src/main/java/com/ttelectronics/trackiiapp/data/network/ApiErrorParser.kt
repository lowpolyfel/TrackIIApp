package com.ttelectronics.trackiiapp.data.network

import org.json.JSONObject
import retrofit2.HttpException

data class ApiErrorDetails(
    val statusCode: Int? = null,
    val message: String,
    val rawBody: String? = null
)

object ApiErrorParser {
    fun toDetails(exception: Throwable): ApiErrorDetails {
        val httpException = exception as? HttpException
            ?: return ApiErrorDetails(message = exception.message ?: "Error desconocido")

        val statusCode = httpException.code()
        val body = httpException.response()?.errorBody()?.string().orEmpty()
        if (body.isBlank()) {
            return ApiErrorDetails(statusCode = statusCode, message = "Error $statusCode", rawBody = null)
        }

        val parsedMessage = runCatching {
            val root = JSONObject(body)
            root.optString("detail")
                .ifBlank { root.optString("title") }
                .ifBlank { root.optString("message") }
                .ifBlank { body }
        }.getOrDefault(body)

        return ApiErrorDetails(statusCode = statusCode, message = parsedMessage, rawBody = body)
    }

    fun readableError(exception: Throwable): String {
        val details = toDetails(exception)
        return if (details.statusCode != null) {
            "Error ${details.statusCode}: ${details.message}"
        } else {
            details.message
        }
    }
}
