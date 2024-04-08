package com.nevexis.backend.schoolManagement.schoolClass

import com.nevexis.backend.schoolManagement.users.StudentView
import com.nevexis.backend.schoolManagement.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SchoolClassController {
    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var userService: UserService

    @GetMapping("/get-school-classes-from-school")
    fun getAllSchoolClassesFromSchoolAndPeriod(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<SchoolClass> = schoolClassService.getAllSchoolClassesFromSchoolAndPeriod(schoolId, periodId)

    @GetMapping("/get-all-students-from-school-class")
    fun getAllStudentsFromSchoolClass(
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<StudentView> {
        return userService.getAllStudentsInSchoolClass(schoolClassId, periodId)
    }

    @GetMapping("/get-school-class-by-id")
    fun getSchoolClassById(
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): SchoolClass? {
        return schoolClassService.getSchoolClassById(schoolClassId)
    }

    @PostMapping("/save-school-class")
    suspend fun saveSchoolClass(
        @RequestBody schoolClass: SchoolClass,
    ) = schoolClassService.saveUpdateSchoolClass(schoolClass)

    @PostMapping("/sync-numbers-in-class")
    suspend fun syncNumbersInClass(@RequestParam schoolClassId: BigDecimal) =
        schoolClassService.synchronizeNumbersInClass(schoolClassId)


}