package com.nevexis.backend.schoolManagement.subject

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SubjectController {
    @Autowired
    private lateinit var subjectService: SubjectService

    @GetMapping("/get-subjects-by-school-class")
    suspend fun getAllSubjectsBySchoolClassId(
        schoolClassId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = subjectService.getAllSubjectsBySchoolClassId(schoolClassId, periodId, schoolId)


    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/get-all-subjects-taught-by-teacher")
    suspend fun fetchAllSubjectsTaughtByTeacher(
        teacherId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) = subjectService.getAllSubjectsTaughtByTeacher(
        teacherId,
        periodId,
        schoolId,
    )

    @GetMapping("/get-subject-by-id")
    suspend fun fetchSubjectsById(
        @RequestParam subjectId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal
    ) = subjectService.getSubjectsById(subjectId, periodId, schoolId)

}