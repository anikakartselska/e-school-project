package com.nevexis.backend.schoolManagement.assignments.examination

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class ExamAnswersController {

    @Autowired
    private lateinit var examAnswersService: ExamAnswersService

    @PostMapping("/save-update-exam-answers")
    fun mergeExamAnswers(
        @RequestBody examAnswers: ExamAnswers,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        examId: BigDecimal,
        user: Principal
    ): ExamAnswers =
        examAnswersService.saveUpdateExamAnswers(examAnswers, schoolId, periodId, examId, user.name.toBigDecimal())


    @PostMapping("/input-grades-on-exam-answers")
    fun inputGradesOnExamAnswers(
        @RequestBody listOfExamAnswers: List<ExamAnswers>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        user: Principal
    ): List<ExamAnswers> =
        examAnswersService.inputGradesToExamAnswers(listOfExamAnswers, schoolId, periodId, user.name.toBigDecimal())

    @PostMapping("/cancel-exam-answers")
    fun cancelExamAnswersList(
        @RequestBody listOfExamAnswers: List<ExamAnswers>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        input2: Boolean,
        examId: BigDecimal,
        user: Principal
    ): List<ExamAnswers> =
        examAnswersService.cancelExamAnswers(
            listOfExamAnswers,
            schoolId,
            periodId,
            input2,
            user.name.toBigDecimal(),
            examId
        )

    @PostMapping("/grade-exam-answers")
    fun gradeExamAnswersList(
        @RequestBody listOfExamAnswers: List<ExamAnswers>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        examId: BigDecimal
    ): List<ExamAnswers> =
        examAnswersService.gradeExamAnswers(listOfExamAnswers, examId, schoolId, periodId)


    @GetMapping("/get-exam-answers")
    fun getExamAnswers(examId: BigDecimal, submittedBy: BigDecimal) =
        examAnswersService.getExamAnswersByExamIdAndSubmittedById(examId, submittedBy)

    @GetMapping("/get-exam-answers-by-exam")
    fun getExamAnswersForExamId(examId: BigDecimal): List<ExamAnswers> {
        examAnswersService.markExamAnswersAsSubmitted(examId)
        return examAnswersService.getExamAnswersForExam(examId)
    }


    @GetMapping("/get-exam-answers-by-id")
    fun getExamAnswersById(id: BigDecimal) =
        examAnswersService.getExamAnswer(id)


}