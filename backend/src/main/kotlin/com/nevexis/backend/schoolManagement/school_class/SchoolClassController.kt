package com.nevexis.backend.schoolManagement.school_class

import com.nevexis.backend.schoolManagement.users.StudentView
import com.nevexis.backend.schoolManagement.users.UserService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SchoolClassController {
    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var userService: UserService

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/get-school-classes-from-school")
    suspend fun fetchAllSchoolClassesFromSchoolAndPeriod(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<SchoolClass> {
        return schoolClassService.getAllSchoolClassesFromSchoolAndPeriod(schoolId, periodId)
    }

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/get-all-students-from-school-class")
    suspend fun getAllStudentsFromSchoolClass(
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<StudentView> {
        return userService.getAllStudentsInSchoolClass(schoolClassId, periodId)
    }

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/fetch-all-school-classes-from-school-and-period-without-plans")
    suspend fun fetchAllSchoolClassesFromSchoolAndPeriodWithoutPlans(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<SchoolClass> {
        return schoolClassService.getAllSchoolClassesFromSchoolAndPeriodWithoutPlans(schoolId, periodId)
    }


    @GetMapping("/get-school-class-by-id")
    suspend fun getSchoolClassById(
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): SchoolClass? {
        return schoolClassService.getSchoolClassById(schoolClassId)
    }

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/save-school-class")
    suspend fun saveSchoolClass(
        @RequestPart schoolClass: String,
        @RequestPart(required = false) studentsFromClassFile: ByteArray? = null,
        principal: Principal
    ) = schoolClassService.saveUpdateSchoolClass(
        Json.decodeFromString(schoolClass),
        studentsFromClassFile,
        principal.name.toBigDecimal()
    )

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/test-upload")
    suspend fun testUpload(
        @RequestPart(required = false) testUpload: ByteArray? = null,
        principal: Principal
    ) = schoolClassService.saveFile(
        testUpload
    )

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/sync-numbers-in-class")
    suspend fun syncNumbersInClass(@RequestParam schoolClassId: BigDecimal, @RequestParam periodId: BigDecimal) =
        schoolClassService.synchronizeNumbersInClass(schoolClassId, periodId)


}