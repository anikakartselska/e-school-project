package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.users.user_security.UserPreferences
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
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

    @PostMapping("/update-current-user-preferences")
    fun updateCurrentUserPreferences(@RequestBody userPreferences: UserPreferences, principal: Principal) =
        userService.updateUserPreferences(userPreferences, principal.name.toBigDecimal())

    @GetMapping("/get-all-users-by-school-id-and-period-id")
    suspend fun getAllUsersBySchoolIdAndPeriodId(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<UserView> = userService.getAllUserViewsBySchool(schoolId, periodId)

    @GetMapping("/get-10-user-views-by-school-matching-search-text")
    fun getAllUserViewsBySchoolPaginated(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        searchText: String,
        principal: Principal
    ) = userService.getLast10UserViews(schoolId, periodId, searchText, principal.name.toBigDecimal())


    @GetMapping("/get-chat-members")
    fun getAllMembersFromChat(
        chatId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = userService.getChatMembers(chatId, schoolId, periodId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
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
    suspend fun fetchUserWithAllItsRolesById(
        @RequestParam id: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        principal: Principal
    ): User = userService.findUserWithAllItsRolesById(id, schoolId, periodId, principal.name)

    @PostMapping("/change-profile-picture")
    suspend fun changeProfilePicture(
        @RequestBody profilePicture: String,
        @RequestParam userId: BigDecimal
    ) {
        userService.changeUserProfilePicture(profilePicture, userId)
    }

    @PostMapping("/get-user-profile-picture")
    suspend fun getProfilePicture(
        @RequestParam userId: BigDecimal
    ): String? {
        return userService.getUserProfilePicture(userId)
    }

    @GetMapping("/get-student-by-id-school-and-period")
    suspend fun getStudentById(
        @RequestParam studentId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = userService.getStudentByIdAndSchoolClass(studentId, schoolClassId, periodId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/get-all-approved-teachers-from-school")
    suspend fun getTeachersFromSchool(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = userService.getAllApprovedTeachersFromSchool(schoolId, periodId)
}
