@file:UseSerializers(
    LocalDateTimeSerializer::class
)

package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.users.StudentView
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.backend.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDateTime

@Serializable
data class Evaluation(
    val id: Int? = null,
    val student: StudentView,
    val subject: Subject,
    val schoolLessonId: Int? = null,
    val evaluationDate: LocalDateTime? = null,
    val evaluationType: EvaluationType,
    val evaluationValue: EvaluationValue,
    val semester: Semester,
    val createdBy: UserView,
    val comment: String? = null
)

enum class EvaluationType {
    GRADE, FEEDBACK, ABSENCE
}
