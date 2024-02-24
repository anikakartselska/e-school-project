package com.nevexis.backend.schoolManagement.security.auth

data class AuthenticationResponse(
    val token: String?,
    val refreshToken:String?,
    val message: String = ""
)