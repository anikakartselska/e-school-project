@file:UseSerializers(
    BigDecimalSerializer::class,
    LocalDateTimeSerializer::class
)

package com.nevexis.backend.schoolManagement.assignments.examination


import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.backend.serializers.BigDecimalSerializer
import com.nevexis.backend.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDateTime

@Serializable
data class ExamAnswers(
    val id: Int? = null,
    val submittedBy: UserView,
    val submittedOn: LocalDateTime? = null,
    val answers: Answers? = null,
    val graded: Boolean = false,
    val grade: Int? = null,
    val inputtedGrade: Boolean = false,
    val examId: Int,
    val submitted: Boolean = false
)