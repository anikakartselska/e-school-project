package com.nevexis.backend.schoolManagement.security.reset_password

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class PasswordResetController {

    @Autowired
    private lateinit var passwordResetService: PasswordResetService

    @PostMapping("/reset-password-request")
    suspend fun resetPasswordRequest(
        @RequestParam email: String
    ): Boolean = passwordResetService.resetPassword(email)

}