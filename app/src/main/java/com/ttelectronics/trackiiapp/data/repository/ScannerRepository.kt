package com.ttelectronics.trackiiapp.data.repository

import com.ttelectronics.trackiiapp.data.local.AppSession
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
import com.ttelectronics.trackiiapp.data.network.ScannerApiService
import retrofit2.HttpException

class ScannerRepository(
    private val api: ScannerApiService,
    private val appSession: AppSession
) {
    suspend fun lookupPart(partNumber: String): PartLookupResponse = api.getPartInfo(partNumber)

    suspend fun getWorkOrderContext(workOrderNumber: String, deviceId: Int, partNumber: String?): WorkOrderContextResponse {
        return api.getWorkOrderContext(workOrderNumber, deviceId, partNumber)
    }

    suspend fun registerScan(
        workOrderNumber: String,
        partNumber: String,
        userId: Int,
        deviceId: Int,
        qtyIn: Int
    ): RegisterScanResponse {
        val effectiveUserId = if (userId > 0) userId else appSession.userId
        val effectiveDeviceId = if (deviceId > 0) deviceId else appSession.deviceId
        return api.registerScan(
            RegisterScanRequest(
                workOrderNumber = workOrderNumber,
                partNumber = partNumber,
                quantity = qtyIn,
                userId = effectiveUserId,
                deviceId = effectiveDeviceId
            )
        )
    }


    suspend fun getErrorCategories(): List<ErrorCategoryResponse> = api.getErrorCategories()

    suspend fun getErrorCodes(categoryId: Int): List<ErrorCodeResponse> = api.getErrorCodes(categoryId)
    suspend fun scrapOrder(workOrderNumber: String, partNumber: String, deviceId: Int, qty: Int, reason: String): ScrapResponse {
        return api.scrap(ScrapRequest(workOrderNumber, partNumber, deviceId, qty, reason))
    }

    suspend fun scrapOrder(request: ScrapOrderRequest): ScrapResponse {
        return api.scrapOrder(request)
    }

    suspend fun reworkOrder(
        workOrderNumber: String,
        partNumber: String,
        quantity: Int,
        locationId: Int,
        isRelease: Boolean,
        reason: String?,
        userId: Int,
        deviceId: Int
    ): ReworkResponse {
        return api.rework(
            ReworkRequest(
                workOrderNumber = workOrderNumber,
                partNumber = partNumber,
                quantity = quantity,
                locationId = locationId,
                isRelease = isRelease,
                reason = reason,
                userId = userId,
                deviceId = deviceId
            )
        )
    }


    suspend fun validateRework(workOrderNumber: String): ReworkValidationResponse {
        return api.validateRework(workOrderNumber)
    }

    suspend fun releaseWipItem(workOrderNumber: String): ReleaseWipItemResponse {
        return api.releaseWipItem(workOrderNumber)
    }

    suspend fun validatePartExists(partNumber: String): Boolean {
        return try {
            val payload = api.getPartInfo(partNumber)
            payload.found ?: true
        } catch (ex: HttpException) {
            if (ex.code() == 404) false else throw ex
        }
    }
}
