package com.ttelectronics.trackiiapp.ui.screens

internal data class StableScanState(
    val lastValue: String = "",
    val stableCount: Int = 0,
    val lastAcceptedAt: Long = 0L
) {
    fun record(value: String): StableScanState {
        val newCount = if (value == lastValue) stableCount + 1 else 1
        return copy(lastValue = value, stableCount = newCount)
    }

    fun canAccept(now: Long, requiredStableReads: Int): Boolean {
        return stableCount >= requiredStableReads && now - lastAcceptedAt > MIN_ACCEPT_INTERVAL_MS
    }

    fun markAccepted(now: Long): StableScanState {
        return copy(lastValue = "", stableCount = 0, lastAcceptedAt = now)
    }

    fun clear(): StableScanState = copy(lastValue = "", stableCount = 0)
}

internal data class BarcodeCandidate(
    val value: String,
    val areaRatio: Float
)

internal fun requiredStableReads(areaRatio: Float): Int {
    return when {
        areaRatio >= HIGH_QUALITY_AREA_RATIO -> 2
        areaRatio >= MEDIUM_QUALITY_AREA_RATIO -> 3
        else -> 4
    }
}

private const val HIGH_QUALITY_AREA_RATIO = 0.06f
private const val MEDIUM_QUALITY_AREA_RATIO = 0.03f
private const val MIN_ACCEPT_INTERVAL_MS = 650L
