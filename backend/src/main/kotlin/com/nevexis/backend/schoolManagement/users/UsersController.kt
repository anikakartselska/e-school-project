package com.nevexis.backend.schoolManagement.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class UsersController {
    @Autowired
    private lateinit var userService: UserService

    @PostMapping("/update-user")
    suspend fun updateUser(@RequestBody user: User, @RequestParam loggedUserId: BigDecimal) =
        userService.updateUser(user, loggedUserId)

    @GetMapping("/get-all-users-by-school-id-and-period-id")
    suspend fun getAllUsersBySchoolIdAndPeriodId(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<UserView> = userService.getAllUserViewsBySchool(schoolId, periodId)

    @GetMapping("/get-user-with-all-roles")
    fun fetchUserWithAllItsRolesById(
        @RequestParam id: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        principal: Principal
    ): User = userService.findUserWithAllItsRolesById(id, schoolId, periodId, principal.name)


//    @GetMapping("/get-user-by-id")
//    fun getUserDetailsById(
//        @RequestParam userId: BigDecimal,
//        @RequestParam periodId: BigDecimal,
//        @RequestParam schoolId: BigDecimal
//    ) = userService.getUserWithDetailsByUserIdAndSchoolId(userId, periodId, schoolId)

//    @GetMapping("/get-all-students-from-class")
//    fun fetchUsersFromClass(
//        @RequestParam schoolClassId: BigDecimal,
//        @RequestParam periodId: BigDecimal
//    ) = userService.getAllStudentsInSchoolClass(schoolClassId, periodId)
}