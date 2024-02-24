package com.nevexis.backend.schoolManagement.requests

import com.nevexis.backend.schoolManagement.users.User
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import java.time.LocalDateTime

interface Request

data class RegistrationRequest(
    val id: Int,
    val requestedByUser: User,
    val requestDate: LocalDateTime,
    val requestStatus: RequestStatus = RequestStatus.PENDING,
    val resolvedByUser: User? = null,
    val resolvedDate: LocalDateTime? = null
) : Request

data class RoleRequest(
    val id: Int,
    val requestedByUser: User,
    val schoolUserRole: SchoolUserRole,
    val requestDate: LocalDateTime,
    val requestStatus: RequestStatus = RequestStatus.PENDING,
    val resolvedByUser: User? = null,
    val resolvedDate: LocalDateTime? = null
) : Request

enum class RequestStatus {
    PENDING,
    REJECTED,
    APPROVED
}