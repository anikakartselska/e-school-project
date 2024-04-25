package com.nevexis.backend.schoolManagement.school_calendar

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CALENDAR_FOR_YEAR
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import java.math.BigDecimal

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

}