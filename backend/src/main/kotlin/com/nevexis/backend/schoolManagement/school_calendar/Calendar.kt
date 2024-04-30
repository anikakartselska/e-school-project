@file:UseSerializers(
    LocalDateSerializer::class,
    LocalDateTimeSerializer::class
)


package com.nevexis.backend.schoolManagement.school_calendar

import com.nevexis.backend.serializers.LocalDateSerializer
import com.nevexis.backend.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

@Serializable
data class Calendar(
    val beginningOfYear: LocalDate,
    val endOfFirstSemester: LocalDate,
    val beginningOfSecondSemester: LocalDate,
    val classToEndOfYearDate: Map<Int, LocalDate>,
    val firstSemesterWeeksCount: Int,
    val classToSecondSemesterWeeksCount: Map<Int, Int>,
    val restDays: List<RestDay>,
    val examDays: List<RestDay>,
    val firstShiftSchedule: DailySchedule,
    val secondShiftSchedule: DailySchedule
)

@Serializable
data class RestDay(
    val from: LocalDate,
    val to: LocalDate,
    val holidayName: String
)

@Serializable
data class DailySchedule(
    val startOfClasses: String,
    val durationOfClasses: Int,
    val breakDuration: Int
)

data class Week(
    val weekNumber: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)

enum class Shift {
    FIRST, SECOND
}