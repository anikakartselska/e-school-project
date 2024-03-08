package com.nevexis.backend.schoolManagement.school_period

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolPeriodSchoolRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PERIOD
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PERIOD_SCHOOL
import org.springframework.stereotype.Service

@Service
class SchoolPeriodService : BaseService() {

    fun fetchAllSchoolPeriods(): List<SchoolPeriod> =
        db.selectFrom(SCHOOL_PERIOD).fetch().into(SchoolPeriod::class.java)

    fun fetchAllSchoolPeriodsWithTheSchoolsTheyAreStarted(): List<SchoolPeriodWithSchoolIds> {
        val allSchoolPEriodSchoolGroupedByPeriodId = fetchAllSchoolPeriodSchool().groupBy { it.schoolPeriodId }
        return db.selectFrom(SCHOOL_PERIOD).fetch().into(SchoolPeriodRecord::class.java).map {
            SchoolPeriodWithSchoolIds(
                id = it.id!!,
                startYear = it.startYear!!,
                endYear = it.endYear!!,
                firstSemester = it.firstSemester!!,
                secondSemester = it.secondSemester!!,
                schoolIds = allSchoolPEriodSchoolGroupedByPeriodId[it.id]?.mapNotNull { it.schoolId } ?: emptyList()
            )
        }
    }

    fun fetchAllSchoolPeriodSchool(): List<SchoolPeriodSchoolRecord> = db.selectFrom(SCHOOL_PERIOD_SCHOOL).fetch()

}