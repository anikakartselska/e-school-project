package com.nevexis.backend.schoolManagement.security

import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import java.math.BigDecimal

data class UserSec(
    val id: BigDecimal,
    val email: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val password: String,
    val username: String,
    val roles: SchoolUserRole?
)