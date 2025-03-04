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

    @GetMapping("/get-exam-answers")
    fun getExamAnswers(examId: BigDecimal, submittedBy: BigDecimal) =
        examAnswersService.getExamAnswersByExamIdAndSubmittedById(examId, submittedBy)

    @GetMapping("/get-exam-answers-by-exam")
    fun getExamAnswersForExamId(examId: BigDecimal) =
        examAnswersService.getExamAnswersForExam(examId)

    @GetMapping("/get-exam-answers-by-id")
    fun getExamAnswersById(id: BigDecimal) =
        examAnswersService.getExamAnswer(id)


}