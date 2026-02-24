package com.ttelectronics.trackiiapp.data.network

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

data class ApiErrorDetails(
    val statusCode: Int? = null,
    val message: String,
    val rawBody: String? = null
)

object ApiErrorParser {
    fun parse(exception: Exception): String? {
        return when (exception) {
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()?.string()
                try {
                    val jsonObject = JSONObject(errorBody ?: "")
                    jsonObject.optString("message").ifBlank {
                        jsonObject.optString("detail").ifBlank { "Error del servidor: ${exception.code()}" }
                    }
                } catch (_: Exception) {
                    "Error del servidor: ${exception.code()}"
                }
            }

            is SocketTimeoutException -> "Tiempo de espera agotado. Revise su red."
            is IOException -> "Error de conexión de red."
            else -> exception.localizedMessage
        }
    }

    fun toDetails(exception: Throwable): ApiErrorDetails {
        val message = if (exception is Exception) parse(exception) else exception.message
        val httpException = exception as? HttpException
        return ApiErrorDetails(
            statusCode = httpException?.code(),
            message = message ?: "Error desconocido",
            rawBody = httpException?.response()?.errorBody()?.string()
        )
    }

    fun readableError(exception: Throwable): String {
        return if (exception is Exception) {
            parse(exception) ?: "Error desconocido"
        } else {
            exception.message ?: "Error desconocido"
        }
    }
}
