package com.nevexis.backend.schoolManagement.users.school_user

import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import com.nevexis.backend.schoolManagement.users.User
import kotlinx.serialization.Serializable

@Serializable
data class SchoolUser(
    val id: Int,
    val school: School,
    val user: User,
    val status: RequestStatus,
    val period: SchoolPeriod
)