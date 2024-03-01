package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import java.math.BigDecimal

data class User(
    val id: BigDecimal?,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val personalNumber: String,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val roles: List<SchoolUserRole>
)

data class OneRoleUser(
    val id: BigDecimal?,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val personalNumber: String,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val role: SchoolUserRole
)

data class UserView(
    val id: BigDecimal,
    val email: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val roles: List<SchoolRole>
)

data class StudentView(
    val id: BigDecimal,
    val email: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val numberInClass: String,
)

enum class SchoolRole {
    ADMIN, TEACHER, STUDENT, PARENT
}

enum class UserStatus {
    CREATED,
    ACTIVE,
    INACTIVE,
    DELETED
}