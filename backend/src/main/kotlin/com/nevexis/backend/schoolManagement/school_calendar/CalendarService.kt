package com.nevexis.backend.schoolManagement.school_calendar

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CALENDAR_FOR_YEAR
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

@Service
class CalendarService : BaseService() {


    fun getSchoolCalendarForSchoolAndPeriod(
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = db.select(SCHOOL_CALENDAR_FOR_YEAR.CALENDAR)
        .from(SCHOOL_CALENDAR_FOR_YEAR).where(
            SCHOOL_CALENDAR_FOR_YEAR.SCHOOL_ID.eq(schoolId)
                .and(SCHOOL_CALENDAR_FOR_YEAR.SCHOOL_PERIOD_ID.eq(periodId))
        ).fetchAnyInto(String::class.java)?.let {
            Json.decodeFromString<Calendar>(it)
        }

    fun getSchoolWeeksForSchoolClass(
        schoolClassName: String,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = getSchoolCalendarForSchoolAndPeriod(schoolId, periodId)?.let {
        it.firstSemesterWeeksCount + (it.classToSecondSemesterWeeksCount[schoolClassName.dropLast(1).toInt()] ?: 0)
    } ?: 0

    fun getWeeksForSchoolClassSchoolAndPeriod(
        schoolClassName: String,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<Week> {
        val calendar = getSchoolCalendarForSchoolAndPeriod(schoolId, periodId) ?: throw SMSError(
            "DATA_NOT_FOUND",
            "There is no calendar for school with id:${schoolId} and period with id:${periodId}"
        )

        return generateSequence(calendar.beginningOfYear) { it.plusDays(1) }
            .takeWhile { !it.isAfter(calendar.classToEndOfYearDate[schoolClassName.dropLast(1).toInt()]!!) }
            .toList()
            .map { date ->
                getStartAndEndOfWeek(date)
            }.distinctBy { it.weekNumber }
    }

    fun getStartAndEndOfWeek(date: LocalDate): Week {
        val week = date.get(WeekFields.ISO.weekOfYear()).toBigDecimal()
        // Adjust to the first day of the week (e.g., Monday for ISO)
        val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // Adjust to the last day of the week (e.g., Sunday for ISO)
        val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        return Week(week.toInt(), startOfWeek, endOfWeek)
    }

}