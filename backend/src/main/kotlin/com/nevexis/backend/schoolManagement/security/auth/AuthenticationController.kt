package com.nevexis.backend.schoolManagement.security.auth

import com.nevexis.backend.schoolManagement.requests.RequestService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriodService
import com.nevexis.backend.schoolManagement.security.JwtService
import com.nevexis.backend.schoolManagement.security.reset_password.PasswordResetService
import com.nevexis.backend.schoolManagement.users.User
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurity
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
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
    private lateinit var schoolPeriodService: SchoolPeriodService

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

    @Autowired
    private lateinit var requestService: RequestService

    @PostMapping("/authenticate")
    suspend fun authenticate(
        @RequestBody request: AuthenticationRequest,
        exchange: ServerWebExchange
    ): ResponseEntity<AuthenticationResponse> {
        exchange.response.addCookie(generateCookie("", "token"))// reset cookie
        exchange.response.addCookie(generateCookie("", "refreshToken"))// reset cookie

        return userSecurityService.findActiveUserByUsername(request.username)
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
        @RequestParam periodId: BigDecimal,
        principal: Principal,
        exchange: ServerWebExchange
    ): ResponseEntity<AuthenticationResponse> {
        exchange.response.addCookie(generateCookie("", "token"))// reset cookie
        exchange.response.addCookie(generateCookie("", "refreshToken"))// reset cookie

        return userSecurityService.findActiveUserById(principal.name.toBigDecimal(), periodId, roleId)
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
    suspend fun getAllUserRoles(@RequestParam userId: BigDecimal, principal: Principal): List<SchoolUserRole> =
        schoolUserRolesService.getAllUserRoles(userId, RequestStatus.APPROVED)


    @GetMapping("/get-all-school-classes")
    suspend fun getAllSchoolClasses(): List<SchoolClass> = schoolClassService.getSchoolClasses()


    @GetMapping("/get-all-periods")
    suspend fun getAllSchoolPeriods(): List<SchoolPeriod> = schoolPeriodService.fetchAllSchoolPeriods()


    @GetMapping("/get-all-school-periods-with-school-ids")
    suspend fun getAllSchoolPeriodsWithTheSchoolsTheyAreStarted() =
        schoolPeriodService.fetchAllSchoolPeriodsWithTheSchoolsTheyAreStarted()


    @GetMapping("/get-all-schools")
    suspend fun getAllSchools(exchange: ServerWebExchange): List<School> = schoolService.getAllSchools()


    @PostMapping("/logout")
    suspend fun logout(exchange: ServerWebExchange) {
        exchange.response.addCookie(generateCookie("", "token"))
        exchange.response.addCookie(generateCookie("", "refreshToken"))
    }

    @GetMapping("/find-user-by-phone-number-period-class")
    fun findStudentByPhoneNumberPeriodAndSchoolClass(
        @RequestParam phoneNumber: String,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal
    ) = userSecurityService.findStudentByPhoneNumberPeriodAndSchoolClass(
        phoneNumber,
        periodId,
        schoolClassId
    )

    @GetMapping("/find-user-with-all-its-roles-by-phone-number")
    fun findUserWithAllItsRolesByPhoneNumber(
        @RequestParam phoneNumber: String
    ) = userSecurityService.findUserWithAllApprovedRolesByPhoneNumber(phoneNumber)

    @PostMapping("/create-requests")
    fun createRequests(
        @RequestBody user: User,
    ) = requestService.createRequests(listOf(user))

    @Autowired
    private lateinit var passwordResetService: PasswordResetService

    @PostMapping("/reset-password-request")
    suspend fun resetPasswordRequest(
        @RequestParam email: String
    ): Boolean = passwordResetService.resetPassword(email)

    @PostMapping("/update-password")
    suspend fun updatePassword(
        @RequestParam newPassword: String,
        @RequestParam passwordResetToken: String
    ) = userSecurityService.updateUserPassword(newPassword, passwordResetToken)


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