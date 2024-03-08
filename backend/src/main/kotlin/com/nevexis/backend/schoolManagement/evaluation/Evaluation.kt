package com.nevexis.backend.schoolManagement.evaluation

import java.math.BigDecimal
import java.time.LocalDate

data class Evaluation(
    val id: BigDecimal,
    val studentRoleId: BigDecimal,
    val subjectId: BigDecimal,
    val schoolLessonId: BigDecimal,
    val evaluationDate: LocalDate,
    val evaluationType: EvaluationType,
    val evaluationValue: EvaluationValue,
    val schoolId: BigDecimal,
    val schoolPeriodId: BigDecimal,
)

enum class EvaluationType {
    GRADE, FEEDBACK, ABSENCE
}