package com.nevexis.backend.schoolManagement.school_plan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SchoolPlanForClassesController {

    @Autowired
    private lateinit var schoolPlanForClassesService: SchoolPlanForClassesService

    @GetMapping("/fetch-all-school-plans-for-school")
    fun fetchAllSchoolPlansForSchool(@RequestParam schoolId: BigDecimal) =
        schoolPlanForClassesService.getAllSchoolPlansForSchool(schoolId)
}