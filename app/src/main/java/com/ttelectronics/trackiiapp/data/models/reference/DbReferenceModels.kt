package com.ttelectronics.trackiiapp.data.models.reference

import java.time.LocalDateTime

data class AreaRef(val id: UInt, val name: String, val active: Boolean)
data class DeviceRef(val id: UInt, val deviceUid: String, val locationId: UInt, val name: String?, val active: Boolean, val userId: UInt?)
data class FamilyRef(val id: UInt, val areaId: UInt, val name: String, val active: Boolean)
data class LocationRef(val id: UInt, val name: String, val active: Boolean)
data class ProductRef(val id: UInt, val subfamilyId: UInt, val partNumber: String, val active: Boolean)
data class RoleRef(val id: UInt, val name: String, val active: Boolean)
data class RouteRef(val id: UInt, val subfamilyId: UInt, val name: String, val version: String, val active: Boolean)
data class RouteStepRef(val id: UInt, val routeId: UInt, val stepNumber: UInt, val locationId: UInt)
data class ScanEventRef(val id: UInt, val wipItemId: UInt, val routeStepId: UInt, val scanType: String, val ts: LocalDateTime)
data class SubfamilyRef(val id: UInt, val familyId: UInt, val name: String, val active: Boolean, val activeRouteId: UInt?)
data class TokenRef(val id: UInt, val name: String, val code: String)
data class UnregisteredPartRef(val partId: Int, val partNumber: String, val creationDateTime: LocalDateTime, val active: Boolean)
data class UserRef(val id: UInt, val username: String, val password: String, val roleId: UInt, val active: Boolean)
data class WipItemRef(val id: UInt, val woOrderId: UInt, val currentStepId: UInt, val status: String, val createdAt: LocalDateTime, val routeId: UInt)
data class WipReworkLogRef(val id: UInt, val wipItemId: UInt, val locationId: UInt, val userId: UInt, val deviceId: UInt, val qty: UInt, val reason: String?, val createdAt: LocalDateTime)
data class WipStepExecutionRef(val id: UInt, val wipItemId: UInt, val routeStepId: UInt, val userId: UInt, val deviceId: UInt, val locationId: UInt, val createdAt: LocalDateTime, val qtyIn: UInt, val qtyScrap: UInt)
data class WorkOrderRef(val id: UInt, val woNumber: String, val productId: UInt, val status: String)
