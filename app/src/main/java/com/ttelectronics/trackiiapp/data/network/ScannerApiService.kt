package com.ttelectronics.trackiiapp.data.network

import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanRequest
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScannerApiService {
    @GET("api/scanner/part/{partNumber}")
    suspend fun getPartInfo(@Path("partNumber") partNumber: String): PartLookupResponse

    @GET("api/scanner/work-orders/{workOrderNumber}/context")
    suspend fun getWorkOrderContext(
        @Path("workOrderNumber") workOrderNumber: String,
        @Query("deviceId") deviceId: Int
    ): WorkOrderContextResponse

    @POST("api/scanner/register")
    suspend fun registerScan(@Body request: RegisterScanRequest): RegisterScanResponse
}
