package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import java.math.BigDecimal

data class UserRegistrationInformation (
    val id: BigDecimal?,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val personalNumber: String?,
    val email: String,
    val phoneNumber: String?,
    val address: String,
    val password: String? = null,
    val schoolRoles: List<SchoolUserRole>
)