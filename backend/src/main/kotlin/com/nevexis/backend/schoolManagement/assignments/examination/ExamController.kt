package com.nevexis.backend.schoolManagement.assignments.examination

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class ExamController {

    @Autowired
    private lateinit var examService: ExamService

    @PostMapping("/save-update-exam")
    fun mergeExam(@RequestBody exam: Exam, schoolId: BigDecimal, periodId: BigDecimal) =
        examService.saveUpdateExam(exam, schoolId, periodId)

    @GetMapping("/get-exam")
    fun getExamById(examId: BigDecimal): Exam? = examService.getExam(examId)

    @PostMapping("/delete-exam")
    fun deleteExamById(examId: BigDecimal) {
        examService.deleteExam(examId)
    }
}