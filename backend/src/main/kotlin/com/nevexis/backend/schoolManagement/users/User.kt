package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class User(
    val id: Int? = null,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val personalNumber: String,
    val gender: Gender,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val password: String? = null,
    val roles: List<SchoolUserRole>? = null
)

@Serializable
data class OneRoleUser(
    val id: Int?,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val personalNumber: String,
    val gender: Gender,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val role: SchoolUserRole
)

@Serializable
data class UserView(
    val id: Int,
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
    val numberInClass: Int?,
)

enum class SchoolRole {
    ADMIN, TEACHER, STUDENT, PARENT
}


enum class Gender {
    MALE, FEMALE
}