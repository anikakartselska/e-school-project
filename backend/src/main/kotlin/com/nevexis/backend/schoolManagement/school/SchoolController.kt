package com.nevexis.backend.schoolManagement.school

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SchoolController {

    @Autowired
    private lateinit var schoolService: SchoolService

    @GetMapping("/get-school-by-id")
    suspend fun getSchoolClassById(
        @RequestParam schoolId: BigDecimal,
    ): School = schoolService.getSchoolById(schoolId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/update-school")
    suspend fun updateSchool(
        @RequestBody school: School
    ) {
        schoolService.updateSchool(school)
    }
}