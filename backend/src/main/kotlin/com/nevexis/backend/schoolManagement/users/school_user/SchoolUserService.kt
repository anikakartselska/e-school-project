package com.nevexis.backend.schoolManagement.users.school_user

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_PERIOD
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolUserService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var schoolService: SchoolService


    fun getSchoolUserById(schoolUserId: BigDecimal, dsl: DSLContext): SchoolUser {
        return recordSelectOnConditionStep(dsl)
            .where(SCHOOL_USER.ID.eq(schoolUserId))
            .fetchAny()?.mapIntoModel(dsl) ?: error("There is no record in SCHOOL_USER with id: $schoolUserId")
    }

    private fun recordSelectOnConditionStep(dsl: DSLContext) =
        dsl.select(SCHOOL_USER.asterisk(), SCHOOL_USER_PERIOD.asterisk())
            .from(SCHOOL_USER)
            .leftJoin(SCHOOL_USER_PERIOD)
            .on(SCHOOL_USER.ID.eq(SCHOOL_USER_PERIOD.SCHOOL_USER_ID))

    fun Record.mapIntoModel(dsl: DSLContext): SchoolUser {
        this.into(SchoolUserRecord::class.java).let {
            val approvedSchoolUserPeriodRecord = this.into(SchoolUserPeriodRecord::class.java)
            return SchoolUser(
                id = it.id!!,
                periodId = approvedSchoolUserPeriodRecord.periodId!!,
                user = userService.getUserByIdWithoutRoles(it.userId!!, dsl) ?: error("User do not exist"),
                status = RequestStatus.valueOf(approvedSchoolUserPeriodRecord.status!!),
                school = schoolService.getSchoolById(it.schoolId!!, dsl)
            )
        }
    }
}