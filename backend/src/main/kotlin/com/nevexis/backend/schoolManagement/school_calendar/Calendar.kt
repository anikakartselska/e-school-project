@file:UseSerializers(
    LocalDateSerializer::class
)

package com.nevexis.backend.schoolManagement.school_calendar

import com.nevexis.backend.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

@Serializable
data class Calendar(
    val beginningOfYear: LocalDate,
    val endOfFirstSemester: LocalDate,
    val beginningOfSecondSemester: LocalDate,
    val classToEndOfYearDate: Map<Int, LocalDate>,
    val restDays: List<RestDay>,
)

@Serializable
data class RestDay(
    val from: LocalDate,
    val to: LocalDate,
    val holidayName: String
)