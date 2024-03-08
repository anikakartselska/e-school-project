package com.nevexis.backend.schoolManagement.users.roles

import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import com.nevexis.backend.schoolManagement.users.DetailsForUser
import com.nevexis.backend.schoolManagement.users.SchoolRole
import java.math.BigDecimal


data class SchoolUserRole(
    val id: BigDecimal,
    val userId: BigDecimal,
    val period: SchoolPeriod,
    val school: School,
    val role: SchoolRole,
    val status: RequestStatus = RequestStatus.PENDING,
    val detailsForUser: DetailsForUser? = null
)
