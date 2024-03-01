package com.nevexis.backend.schoolManagement.users.school_user

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER
import org.jooq.DSLContext
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
        return dsl.selectFrom(SCHOOL_USER).where(SCHOOL_USER.ID.eq(schoolUserId))
            .fetchAny()?.mapIntoModel(dsl) ?: error("There is no record in SCHOOL_USER with id: $schoolUserId")
    }

    fun SchoolUserRecord.mapIntoModel(dsl: DSLContext) =
        SchoolUser(
            id = this.id!!,
            periodId = this.periodId!!,
            user = userService.getUserByIdWithoutRoles(this.userId!!, dsl) ?: error("User do not exist"),
            status = RequestStatus.valueOf(this.status!!),
            school = schoolService.getSchoolById(this.schoolId!!, dsl)
        )

}