package com.nevexis.backend.schoolManagement.school_plan

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
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

    @GetMapping("/fetch-plan-by-id")
    fun fetchPlanById(
        @RequestParam planForClassesId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = schoolPlanForClassesService.getPlanById(planForClassesId, schoolId, periodId)

    @PostMapping("/delete-school-plans-for-classes")
    fun deleteSchoolPlansForClasses(
        @RequestBody schoolPlanForClasses: SchoolPlanForClasses,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = schoolPlanForClassesService.deleteSchoolPlansForClasses(schoolPlanForClasses, schoolId, periodId)


    @PostMapping("/merge-school-plans-for-classes")
    fun mergeSchoolPlansForClasses(
        @RequestBody schoolPlanForClasses: SchoolPlanForClasses,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): SchoolPlanForClasses {
        return schoolPlanForClassesService.saveUpdateSchoolPlansForClasses(schoolPlanForClasses, schoolId, periodId)
    }

    @PostMapping("/get-plan-for-school-class")
    fun getPlanForSchoolClass(@RequestBody schoolClass: SchoolClass) =
        schoolPlanForClassesService.fetchPlanForSchoolClass(schoolClass)
}