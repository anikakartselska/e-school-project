package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class EvaluationController {

    @Autowired
    private lateinit var evaluationService: EvaluationService

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/fetch-all-student-subject-evaluations-from-school-class")
    suspend fun fetchAllStudentSubjectEvaluationsFromSchoolClass(
        @RequestBody schoolClass: SchoolClass,
        @RequestParam evaluationType: EvaluationType
    ): Map<Int, Map<Int, List<Evaluation>>> =
        evaluationService.getAllStudentSubjectEvaluationsFromSchoolClass(schoolClass, evaluationType)

    @PostMapping("/get-student-subjects-with-evaluation")
    suspend fun getStudentSubjectsAndTheirEvaluations(
        @RequestParam studentId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal
    ) = evaluationService.getAllEvaluationsForStudent(studentId, periodId, schoolId, schoolClassId)


    @PostMapping("/get-evaluation-for-subject-and-school-class")
    suspend fun getEvaluationForSubjectAndSchoolClass(
        @RequestParam subjectId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam schoolLessonId: BigDecimal? = null
    ) = evaluationService.getAllEvaluationsForSubjectAnSchoolClass(
        subjectId,
        periodId,
        schoolId,
        schoolClassId,
        schoolLessonId
    )

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/save-evaluations")
    suspend fun saveEvaluations(
        @RequestBody evaluations: List<StudentWithEvaluationDTO>,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam comment: String
    ) = evaluationService.saveEvaluations(evaluations, periodId, schoolId, comment)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/save-evaluation")
    suspend fun saveEvaluation(
        @RequestBody evaluation: Evaluation,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal
    ) = evaluationService.saveEvaluation(evaluation, periodId, schoolId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/update-evaluations")
    suspend fun updateEvaluations(
        @RequestBody evaluations: List<StudentWithEvaluationDTO>,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
    ) = evaluationService.updateEvaluations(evaluations, periodId, schoolId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/update-evaluation")
    suspend fun updateEvaluation(
        @RequestBody evaluation: Evaluation,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
    ) = evaluationService.updateEvaluation(evaluation, periodId, schoolId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/delete-evaluation")
    suspend fun deleteEvaluation(
        @RequestBody evaluation: Evaluation,
        @RequestParam periodId: BigDecimal,
        @RequestParam schoolId: BigDecimal,
    ) = evaluationService.deleteEvaluation(evaluation, periodId, schoolId)

}