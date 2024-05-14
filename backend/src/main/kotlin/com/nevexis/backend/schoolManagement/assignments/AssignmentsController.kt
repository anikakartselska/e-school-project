package com.nevexis.backend.schoolManagement.assignments

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class AssignmentsController {

    @Autowired
    private lateinit var assignmentsService: AssignmentsService

    @PostMapping("/merge-assignments")
    fun mergeAssignments(
        @RequestBody assignments: Assignments,
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): Assignments = assignmentsService.saveUpdateAssignments(assignments, schoolClassId, schoolId, periodId)

    @PostMapping("/delete-assignments")
    fun deleteAssignments(
        @RequestBody assignments: Assignments,
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) {
        assignmentsService.deleteAssignments(assignments, schoolClassId, schoolId, periodId)
    }

    @GetMapping("/fetch-all-assignments-for-school-class-period-and-school")
    fun fetchAllAssignmentsForSchoolClassPeriodAndSchool(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam schoolLessonId: BigDecimal? = null
    ) = assignmentsService.getAllAssignmentsForSchoolClassPeriodAndSchool(
        schoolId,
        periodId,
        schoolClassId,
        schoolLessonId
    )
}