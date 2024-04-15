package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class EvaluationController {

    @Autowired
    private lateinit var evaluationService: EvaluationService

    @PostMapping("/fetch-all-student-subject-evaluations-from-school-class")
    fun fetchAllStudentSubjectEvaluationsFromSchoolClass(
        @RequestBody schoolClass: SchoolClass,
        @RequestParam evaluationType: EvaluationType
    ): Map<BigDecimal, Map<BigDecimal, List<Evaluation>>> =
        evaluationService.getAllStudentSubjectEvaluationsFromSchoolClass(schoolClass, evaluationType)

    @PostMapping("/get-student-subjects-with-evaluation")
    fun getStudentSubjectsAndTheirEvaluations(
        @RequestParam studentId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal
    ) = evaluationService.getAllEvaluationsForStudent(studentId, periodId, schoolId, schoolClassId)

    @PostMapping("/get-evaluation-for-subject-and-school-class")
    fun getEvaluationForSubjectAndSchoolClass(
        @RequestParam subjectId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal
    ) = evaluationService.getAllEvaluationsForSubjectAnSchoolClass(
        subjectId,
        periodId,
        schoolId,
        schoolClassId
    )

    @PostMapping("/save-evaluations")
    fun saveEvaluations(
        @RequestBody evaluations: List<Evaluation>,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
    ) = evaluationService.saveEvaluations(evaluations, periodId, schoolId)
}