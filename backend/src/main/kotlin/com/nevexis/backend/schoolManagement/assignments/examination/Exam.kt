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
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class Exam(
    val id: BigDecimal? = null,
    val createdBy: UserView,
    val createdOn: LocalDateTime? = null,
    val examNote: String? = null,
    val questions: Questions? = null,
    val gradingScale: GradingScale? = null,
    val lookAtExamAfterGrading: Boolean = false,
    val startTimeOfExam: LocalDateTime? = null,
    val endTimeOfExam: LocalDateTime? = null,
)


