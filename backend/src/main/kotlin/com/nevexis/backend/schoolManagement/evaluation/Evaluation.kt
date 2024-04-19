@file:UseSerializers(
    LocalDateSerializer::class
)

package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.users.StudentView
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.backend.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

@Serializable
data class Evaluation(
    val id: Int? = null,
    val student: StudentView,
    val subject: Subject,
    val schoolLessonId: Int? = null,
    val evaluationDate: LocalDate,
    val evaluationType: EvaluationType,
    val evaluationValue: EvaluationValue,
    val semester: Semester,
    val createdBy: UserView
)

enum class EvaluationType {
    GRADE, FEEDBACK, ABSENCE
}
