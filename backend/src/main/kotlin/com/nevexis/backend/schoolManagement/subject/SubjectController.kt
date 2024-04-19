package com.nevexis.backend.schoolManagement.subject

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SubjectController {
    @Autowired
    private lateinit var subjectService: SubjectService

    @GetMapping("/get-subjects-by-school-class")
    fun getAllSubjectsBySchoolClassId(
        schoolClassId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = subjectService.getAllSubjectsBySchoolClassId(schoolClassId, periodId, schoolId)

    @GetMapping("/get-subjects-by-school-class-for-school-year")
    fun getAllSubjectsBySchoolClassIdForSchoolYear(
        schoolClassId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = subjectService.getAllSubjectsBySchoolClassId(schoolClassId, periodId, schoolId)


    @GetMapping("/get-all-subjects-taught-by-teacher")
    fun fetchAllSubjectsTaughtByTeacher(
        teacherId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) = subjectService.getAllSubjectsTaughtByTeacher(
        teacherId,
        periodId,
        schoolId,
    )

    @GetMapping("/get-subject-by-id")
    fun fetchSubjectsById(
        @RequestParam subjectId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal
    ) = subjectService.getSubjectsById(subjectId, periodId, schoolId)

}