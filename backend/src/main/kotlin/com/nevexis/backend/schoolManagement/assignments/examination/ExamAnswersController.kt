package com.nevexis.backend.schoolManagement.assignments.examination

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

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
        examId: BigDecimal
    ): ExamAnswers = examAnswersService.saveUpdateExamAnswers(examAnswers, schoolId, periodId, examId)

    @GetMapping("/get-exam-answers")
    fun getExamAnswers(examId: BigDecimal, submittedBy: BigDecimal) =
        examAnswersService.getExamAnswersByExamIdAndSubmittedById(examId, submittedBy)

}