package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.users.StudentView
import com.nevexis.backend.schoolManagement.users.UserView
import java.math.BigDecimal
import java.time.LocalDate

data class Evaluation(
    val id: BigDecimal,
    val student: StudentView,
    val subject: Subject,
    val schoolLessonId: BigDecimal? = null,
    val evaluationDate: LocalDate,
    val evaluationType: EvaluationType,
    val evaluationValue: EvaluationValue,
    val semester: Semester,
    val createdBy: UserView
)

enum class EvaluationType {
    GRADE, FEEDBACK, ABSENCE
}
