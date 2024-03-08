package com.nevexis.backend.schoolManagement.subject

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SubjectController {
    @Autowired
    private lateinit var subjectService: SubjectService

    @GetMapping("/get-student-subjects-with-evaluation")
    fun getStudentSubjectsAndTheirEvaluations(
        schoolClassId: BigDecimal,
        studentId: BigDecimal,//TODO studentRoleId
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = subjectService.getAllStudentSubjectsAndTheirEvaluations(schoolClassId, studentId, periodId, schoolId)

    @GetMapping("/get-subjects-by-school-class")
    fun getAllSubjectsBySchoolClassId(
        schoolClassId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = subjectService.getAllSubjectsBySchoolClassId(schoolClassId, periodId, schoolId)

    @GetMapping("/get-all-evaluations-from-subject-in-school-class")
    fun fetchSchoolClassStudentsAndTheirEvaluations(
        subjectId: BigDecimal,
        schoolClassId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) = subjectService.getSubjectFromSchoolClassAndItsEvaluations(
        subjectId,
        schoolClassId,
        periodId,
        schoolId
    )

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
}