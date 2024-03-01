package com.nevexis.backend.schoolManagement.users.school_user

import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.users.User
import java.math.BigDecimal

data class SchoolUser(
    val id: BigDecimal,
    val school: School,
    val user: User,
    val status: RequestStatus,
    val periodId: BigDecimal
)