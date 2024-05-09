@file:UseSerializers(
    LocalDateTimeSerializer::class
)

package com.nevexis.backend.schoolManagement.assignments

import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.backend.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDateTime

@Serializable
data class Assignments(
    val id: Int? = null,
    val createdBy: UserView,
    val createdOn: LocalDateTime? = null,
    val text: String,
    val semester: Semester,
    val assignmentType: AssignmentType,
    val assignmentValue: AssignmentValue
)


enum class AssignmentType {
    EXAMINATION, HOMEWORK, EVENT
}