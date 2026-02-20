package com.ttelectronics.trackiiapp.data.network

import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ScannerApiService {
    @GET("api/scanner/part/{partNumber}")
    suspend fun getPartInfo(@Path("partNumber") partNumber: String): PartLookupResponse
}
