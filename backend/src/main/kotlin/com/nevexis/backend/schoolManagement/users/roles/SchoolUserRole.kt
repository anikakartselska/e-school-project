package com.nevexis.backend.schoolManagement.users.roles

import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import com.nevexis.backend.schoolManagement.users.DetailsForUser
import com.nevexis.backend.schoolManagement.users.SchoolRole
import kotlinx.serialization.Serializable

@Serializable
data class SchoolUserRole(
    val id: Int? = null,
    val userId: Int? = null,
    val period: SchoolPeriod,
    val school: School,
    val role: SchoolRole,
    val status: RequestStatus = RequestStatus.PENDING,
    val detailsForUser: DetailsForUser? = null
)
