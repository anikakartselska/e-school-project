package com.nevexis.backend.schoolManagement.school_period

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PERIOD
import org.springframework.stereotype.Service

@Service
class SchoolPeriodService : BaseService() {

    fun fetchAllSchoolPeriods(): List<SchoolPeriod> =
        db.selectFrom(SCHOOL_PERIOD).fetch().into(SchoolPeriod::class.java)
}