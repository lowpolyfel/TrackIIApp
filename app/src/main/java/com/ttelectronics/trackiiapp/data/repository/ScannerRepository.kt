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
    suspend fun lookupPart(partNumber: String): PartLookupResponse = api.getPartInfo(partNumber.trim())

    suspend fun getWorkOrderContext(workOrderNumber: String, deviceId: Int): WorkOrderContextResponse {
        return api.getWorkOrderContext(workOrderNumber.trim(), deviceId)
    }

    suspend fun registerEntryScan(workOrderNumber: String, partNumber: String, deviceId: Int, qtyIn: Int?): RegisterScanResponse {
        return api.registerScan(
            RegisterScanRequest(
                workOrderNumber = workOrderNumber,
                partNumber = partNumber.trim(),
                deviceId = deviceId,
                scanType = ScanType.ENTRY,
                qtyIn = qtyIn
            )
        )
    }

    suspend fun scrapOrder(workOrderNumber: String, partNumber: String, deviceId: Int, qty: Int, reason: String): ScrapResponse {
        return api.scrap(ScrapRequest(workOrderNumber.trim(), partNumber.trim(), deviceId, qty, reason.trim()))
    }

    suspend fun reworkOrder(workOrderNumber: String, partNumber: String, deviceId: Int, location: String, reason: String?): ReworkResponse {
        return api.rework(ReworkRequest(workOrderNumber.trim(), partNumber.trim(), deviceId, location.trim(), reason?.trim()))
    }

    suspend fun validateWorkOrder(workOrderNumber: String, deviceId: Int): Boolean {
        return try {
            api.getWorkOrderContext(workOrderNumber.trim(), deviceId)
            true
        } catch (ex: HttpException) {
            if (ex.code() == 404) false else throw ex
        }
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
