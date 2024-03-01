package com.nevexis.backend.schoolManagement.security.auth

import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClass
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClassService
import com.nevexis.backend.schoolManagement.security.JwtService
import com.nevexis.backend.schoolManagement.security.user_security.UserSecurity
import com.nevexis.backend.schoolManagement.security.user_security.UserSecurityService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import java.math.BigDecimal
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/auth")
class AuthenticationController {

    @Autowired
    private lateinit var userSecurityService: UserSecurityService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var schoolService: SchoolService

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var schoolUserRolesService: SchoolRolesService

    @PostMapping("/authenticate")
    suspend fun authenticate(
        @RequestBody request: AuthenticationRequest,
        exchange: ServerWebExchange
    ): ResponseEntity<AuthenticationResponse> {
        exchange.response.addCookie(generateCookie("", "token"))// reset cookie
        exchange.response.addCookie(generateCookie("", "refreshToken"))// reset cookie

        return userSecurityService.findUserByUsername(request.username)
            ?.takeIf { userDetails -> passwordEncoder.matches(request.password, userDetails.password) }
            ?.let { userDetails ->
                val token = jwtService.generateToken(userDetails)
                val refreshToken = jwtService.generateRefreshToken(userDetails)
                exchange.response.addCookie(generateCookie(token, "token"))
                exchange.response.addCookie(generateCookie(refreshToken, "refreshToken"))
                val frontendUser = userDetails.user.copy(password = null)
                ResponseEntity.ok(
                    AuthenticationResponse.Success(
                        frontendUser
                    )
                )
            } ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(AuthenticationResponse.Error(AuthenticationResult.ERROR))
    }

    @PostMapping("/authenticate-after-selected-school")
    fun authenticateAfterSelectedSchool(
        @RequestParam roleId: BigDecimal,
        principal: Principal,
        exchange: ServerWebExchange
    ): ResponseEntity<AuthenticationResponse> {
        exchange.response.addCookie(generateCookie("", "token"))// reset cookie
        exchange.response.addCookie(generateCookie("", "refreshToken"))// reset cookie

        return userSecurityService.findUserByUsername(principal.name, roleId)
            ?.let { userDetails ->
                val token = jwtService.generateToken(userDetails)
                val refreshToken = jwtService.generateRefreshToken(userDetails)
                exchange.response.addCookie(generateCookie(token, "token"))
                exchange.response.addCookie(generateCookie(refreshToken, "refreshToken"))
                val frontendUser = userDetails.user.copy(password = null)
                ResponseEntity.ok(
                    AuthenticationResponse.Success(
                        frontendUser
                    )
                )
            } ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(AuthenticationResponse.Error(AuthenticationResult.ERROR))
    }

    @GetMapping("/get-all-user-roles")
    suspend fun getAllUserRoles(principal: Principal): List<SchoolUserRole> {
        val userId = userSecurityService.findUserByUsername(principal.name)?.user?.id
            ?: error("User with username ${principal.name} does not exist")
        return schoolUserRolesService.getAllUserRoles(userId)
    }

    @GetMapping("/get-all-school-classes")
    suspend fun getSchoolClassesFromSchool(): List<SchoolClass> {
        return schoolClassService.getSchoolClasses()
    }

    @GetMapping("/get-all-schools")
    suspend fun getAllSchools(exchange: ServerWebExchange): List<School> {
        return schoolService.getAllSchools()
    }


    @PostMapping("/logout")
    suspend fun logout(exchange: ServerWebExchange) {
        exchange.response.addCookie(generateCookie("", "token"))
        exchange.response.addCookie(generateCookie("", "refreshToken"))
    }

    private fun generateCookie(token: String, cookieName: String) = ResponseCookie.from(cookieName, token)
        .path("/")
        .httpOnly(true)
        .secure(true)
        .sameSite("strict")
        .build()

    sealed class AuthenticationResponse {
        data class Success(val user: UserSecurity) : AuthenticationResponse()
        data class Error(val result: AuthenticationResult) : AuthenticationResponse()
    }

    enum class AuthenticationResult {
        ERROR
    }
}