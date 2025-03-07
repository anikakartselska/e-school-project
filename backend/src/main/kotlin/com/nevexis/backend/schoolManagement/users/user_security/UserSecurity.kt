package com.nevexis.backend.schoolManagement.users.user_security

import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserSecurity(
    val id: Int,
    val email: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val password: String?,
    val username: String,
    val role: SchoolUserRole?,
    val preferences: UserPreferences? = null
)

@Serializable
data class UserPreferences(
    val enableDarkMode: Boolean = false
)