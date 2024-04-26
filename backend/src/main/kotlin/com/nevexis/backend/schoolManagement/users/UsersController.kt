package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class UsersController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userSecurityService: UserSecurityService

    @PostMapping("/update-user")
    suspend fun updateUser(@RequestBody user: User, @RequestParam loggedUserId: BigDecimal) =
        userService.updateUser(user, loggedUserId)

    @GetMapping("/get-all-users-by-school-id-and-period-id")
    suspend fun getAllUsersBySchoolIdAndPeriodId(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<UserView> = userService.getAllUserViewsBySchool(schoolId, periodId)


    @GetMapping("/get-all-teachers-that-do-not-have-school-class")
    suspend fun getAllTeachersWhichDoNotHaveSchoolClassForSchoolAndPeriod(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<UserView> = userService.getAllTeachersWhichDoNotHaveSchoolClassForSchoolAndPeriod(schoolId, periodId)

    @PostMapping("/change-user-password")
    suspend fun changeUserPassword(
        @RequestParam newPassword: String,
        @RequestParam oldPassword: String,
        principal: Principal
    ) = userSecurityService.changeUserPassword(principal.name.toBigDecimal(), newPassword, oldPassword)


    @GetMapping("/get-user-with-all-roles")
    fun fetchUserWithAllItsRolesById(
        @RequestParam id: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        principal: Principal
    ): User = userService.findUserWithAllItsRolesById(id, schoolId, periodId, principal.name)

    @PostMapping("/change-profile-picture")
    fun changeProfilePicture(
        @RequestPart profilePicture: ByteArray,
        @RequestParam userId: BigDecimal
    ) {
        userService.changeUserProfilePicture(profilePicture, userId)
    }

    @PostMapping("/get-user-profile-picture")
    fun generateReportExcel(
        @RequestParam userId: BigDecimal
    ): ResponseEntity<ByteArrayResource>? {

        val resource = userService.getUserProfilePicture(userId)?.let {
            ByteArrayResource(
                it
            )
        }

        return resource?.contentLength()?.let {
            ResponseEntity.status(HttpStatus.OK).headers(
                HttpHeaders().apply {
                    set(
                        HttpHeaders.CONTENT_DISPOSITION,
                        """attachment; filename="profile-picture${userId}.jpeg""""
                    )
                }
            ).contentLength(it)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource)
        }
    }

    @GetMapping("/get-student-by-id-school-and-period")
    fun getStudentById(
        @RequestParam studentId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = userService.getStudentByIdAndSchoolClass(studentId, schoolClassId, periodId)

}
//{
//    "Литература": 5,
//    "Биология": 3,
//    "Математика": 4,
//    "Информатика": 3,
//    "Информационни технологии": 6,
//    "Философия": 2,
//    "Музика": 4,
//    "Изобразително изкуство": 2,
//    "Физическо възпитание и спорт": 2
//}
