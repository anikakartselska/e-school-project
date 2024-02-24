package com.nevexis.backend.schoolManagement.security.auth

data class AuthenticationRequest(
    val username: String,
    val password: String
)