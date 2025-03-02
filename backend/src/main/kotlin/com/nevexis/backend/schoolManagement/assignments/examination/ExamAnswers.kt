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
data class ExamAnswers(
    val id: BigDecimal? = null,
    val submittedBy: UserView,
    val submittedOn: LocalDateTime? = null,
    val answers: Answers? = null,
    val graded: Boolean = false
)