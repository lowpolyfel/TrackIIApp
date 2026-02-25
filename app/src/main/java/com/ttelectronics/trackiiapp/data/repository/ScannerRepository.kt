package com.ttelectronics.trackiiapp.data.repository

import com.ttelectronics.trackiiapp.data.models.enums.ScanType
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanRequest
import com.ttelectronics.trackiiapp.data.models.scanner.RegisterScanResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ReworkRequest
import com.ttelectronics.trackiiapp.data.models.scanner.ReworkResponse
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapRequest
import com.ttelectronics.trackiiapp.data.models.scanner.ScrapResponse
import com.ttelectronics.trackiiapp.data.models.scanner.WorkOrderContextResponse
import com.ttelectronics.trackiiapp.data.network.ScannerApiService
import retrofit2.HttpException

class ScannerRepository(private val api: ScannerApiService) {
    suspend fun lookupPart(partNumber: String): PartLookupResponse = api.getPartInfo(partNumber)

    suspend fun getWorkOrderContext(workOrderNumber: String, deviceId: Int): WorkOrderContextResponse {
        return api.getWorkOrderContext(workOrderNumber, deviceId)
    }

    suspend fun registerScan(
        workOrderNumber: String,
        partNumber: String,
        deviceId: Int,
        scanType: ScanType,
        qtyIn: Int?
    ): RegisterScanResponse {
        return api.registerScan(
            RegisterScanRequest(
                workOrderNumber = workOrderNumber,
                partNumber = partNumber,
                deviceId = deviceId,
                scanType = scanType,
                qtyIn = qtyIn
            )
        )
    }

    suspend fun scrapOrder(workOrderNumber: String, partNumber: String, deviceId: Int, qty: Int, reason: String): ScrapResponse {
        return api.scrap(ScrapRequest(workOrderNumber, partNumber, deviceId, qty, reason))
    }

    suspend fun reworkOrder(workOrderNumber: String, partNumber: String, deviceId: Int, location: String, reason: String?): ReworkResponse {
        return api.rework(ReworkRequest(workOrderNumber, partNumber, deviceId, location, reason))
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
