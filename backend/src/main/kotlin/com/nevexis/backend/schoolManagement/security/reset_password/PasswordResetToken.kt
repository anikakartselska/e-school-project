package com.nevexis.backend.schoolManagement.security.reset_password

import com.nevexis.backend.schoolManagement.users.user_security.UserSecurity
import java.time.LocalDateTime

data class PasswordResetToken(
    val id: Int,

    val token: String,

    val user: UserSecurity,

    val expiryDate: LocalDateTime
)