package com.nevexis.backend.schoolManagement.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class UsersController {
    @Autowired
    private lateinit var userService: UserService

    @GetMapping("/get-all-users-by-school-id")
    suspend fun getAllUsersBySchoolId(
        @RequestParam schoolId: BigDecimal
    ) = userService.getAllUserViewsBySchool(schoolId)

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