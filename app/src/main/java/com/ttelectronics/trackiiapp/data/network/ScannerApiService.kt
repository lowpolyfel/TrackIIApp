package com.ttelectronics.trackiiapp.data.network

import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCategoryResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ErrorCodeResponse
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanRequest
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ReleaseWipItemResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ReworkRequest
import com.ttelectronics.trackiiapp.data.models.scanner.ReworkResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ReworkValidationResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapOrderRequest
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapRequest
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ScannerApiService {
    @GET("api/scanner/part/{partNumber}")
    suspend fun getPartInfo(@Path("partNumber") partNumber: String): PartLookupResponse

    @GET("api/scanner/work-orders/{workOrderNumber}/context")
    suspend fun getWorkOrderContext(
        @Path("workOrderNumber") workOrderNumber: String,
        @Query("deviceId") deviceId: Int,
        @Query("partNumber") partNumber: String?
    ): WorkOrderContextResponse

    @POST("api/scanner/register")
    suspend fun registerScan(@Body request: RegisterScanRequest): RegisterScanResponse

    @GET("api/scanner/error-categories")
    suspend fun getErrorCategories(): List<ErrorCategoryResponse>

    @GET("api/scanner/error-categories/{id}/codes")
    suspend fun getErrorCodes(@Path("id") categoryId: Int): List<ErrorCodeResponse>

    @POST("api/scanner/scrap")
    suspend fun scrap(@Body request: ScrapRequest): ScrapResponse

    @POST("api/scanner/scrap")
    suspend fun scrapOrder(@Body request: ScrapOrderRequest): ScrapResponse

    @POST("api/scanner/partial-scrap")
    suspend fun partialScrap(@Body request: ScrapOrderRequest): ScrapResponse

    @POST("api/scanner/rework")
    suspend fun rework(@Body request: ReworkRequest): ReworkResponse

    @GET("api/scanner/ValidateRework/{workOrderNumber}")
    suspend fun validateRework(@Path("workOrderNumber") workOrderNumber: String): ReworkValidationResponse

    @PUT("api/scanner/ReleaseWipItem/{workOrderNumber}")
    suspend fun releaseWipItem(@Path("workOrderNumber") workOrderNumber: String): ReleaseWipItemResponse
}
