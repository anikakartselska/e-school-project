package com.nevexis.backend.schoolManagement.users.school_user

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.UserStatus
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_PERIOD
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolUserService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var schoolService: SchoolService

    fun createSchoolUsersFromListOfSchoolUserRoles(
        userId: BigDecimal,
        schoolUserRoles: List<SchoolUserRole>,
        dsl: DSLContext
    ): List<BigDecimal> {
        return dsl.transactionResult { transaction ->
            schoolUserRoles.groupBy { Pair(it.school, it.period) }
                .keys.map { (school, period) ->
                    (transaction.dsl().fetchOne(
                        SCHOOL_USER,
                        SCHOOL_USER.SCHOOL_ID.eq(school.id.toBigDecimal()),
                        SCHOOL_USER.USER_ID.eq(userId)
                    ) ?: transaction.dsl().newRecord(SCHOOL_USER).apply {
                        this.schoolId = school.id.toBigDecimal()
                        this.userId = userId
                        id = getSchoolUserSeqNextVal()
                    }).let { schoolUser ->
                        val existingRecordFromDataBase = transaction.dsl().fetchOne(
                            SCHOOL_USER_PERIOD,
                            SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(schoolUser.id),
                            SCHOOL_USER_PERIOD.PERIOD_ID.eq(period.id.toBigDecimal())
                        )
                        schoolUser to if (existingRecordFromDataBase != null) {
                            existingRecordFromDataBase to false
                        } else {
                            transaction.dsl().newRecord(SCHOOL_USER_PERIOD).apply {
                                this.schoolUserId = schoolUser.id
                                this.periodId = period.id.toBigDecimal()
                                id = getSchoolUserPeriodSeqNextVal()
                                status = UserStatus.CREATED.name
                            } to true
                        }

                    }
                }.let { listOfPairs ->
                    val schoolUserRecords = listOfPairs.map { it.first }
                    val schoolUserPeriodRecords = listOfPairs.map { it.second }
                    val newlyCreatedUserPeriodRecords = schoolUserPeriodRecords.filter { it.second }
                    transaction.dsl().batchStore(schoolUserRecords).execute()
                    transaction.dsl().batchStore(schoolUserPeriodRecords.map { it.first }).execute()
                    newlyCreatedUserPeriodRecords.map { it.first.id!! }
                }
        }
    }

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

    fun getSchoolUserSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getSchoolUserPeriodSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_PERIOD_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

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