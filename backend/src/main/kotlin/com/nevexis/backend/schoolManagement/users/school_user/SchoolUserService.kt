package com.nevexis.backend.schoolManagement.users.school_user

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.AdditionalRequestInformation
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.requests.RequestValueJson
import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.school_period.mapSchoolPeriodRecordToSchoolPeriod
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.`demo-project`.jooq.tables.records.SchoolPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
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
    ): List<AdditionalRequestInformation> {
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
                            SCHOOL_USER_PERIOD.PERIOD_ID.eq(period.id?.toBigDecimal())
                        )
                        schoolUser to if (existingRecordFromDataBase != null) {
                            existingRecordFromDataBase to false
                        } else {
                            transaction.dsl().newRecord(SCHOOL_USER_PERIOD).apply {
                                this.schoolUserId = schoolUser.id
                                this.periodId = period.id?.toBigDecimal()
                                id = getSchoolUserPeriodSeqNextVal()
                                status = RequestStatus.PENDING.name
                            } to true
                        }

                    }
                }.let { listOfPairs ->
                    val schoolUserRecords = listOfPairs.map { it.first }
                    val schoolUserPeriodRecords = listOfPairs.map { it.second }
                    val newlyCreatedUserPeriodRecords = listOfPairs.filter { it.second.second }
                    transaction.dsl().batchStore(schoolUserRecords).execute()
                    transaction.dsl().batchStore(schoolUserPeriodRecords.map { it.first }).execute()
                    newlyCreatedUserPeriodRecords.map {
                        AdditionalRequestInformation(
                            valueId = it.second.first.id!!,
                            periodId = it.second.first.periodId!!,
                            schoolId = it.first.schoolId!!
                        )
                    }
                }
        }
    }

    fun getSchoolUsersByListOfSchoolUserPeriodIds(
        schoolUserPeriodIds: List<BigDecimal>,
        dsl: DSLContext
    ): Map<BigDecimal, SchoolUser> {
        return recordSelectConditionStep(dsl).where(
            SCHOOL_USER_PERIOD.ID.`in`(schoolUserPeriodIds)
        ).fetch()
            .associate {
                it.get(SCHOOL_USER_PERIOD.ID)!! to mapRecordToModel(it)
            }

    }

    private fun mapRecordToModel(it: Record): SchoolUser {
        val schoolUserPeriodRecord = it.into(SchoolUserPeriodRecord::class.java)
        val schoolUserRecord = it.into(SchoolUserRecord::class.java)
        val schoolPeriod = it.into(SchoolPeriodRecord::class.java).mapSchoolPeriodRecordToSchoolPeriod()
        val school = it.into(SchoolRecord::class.java).into(School::class.java)
        val user = userService.mapUserRecordToUserModel(it, emptyList())
        return SchoolUser(
            id = schoolUserRecord.id!!.toInt(),
            school = school,
            user = user,
            status = RequestStatus.valueOf(schoolUserPeriodRecord.status!!),
            period = schoolPeriod
        )
    }


    private fun recordSelectConditionStep(dsl: DSLContext) = dsl.select(
        SCHOOL_USER_PERIOD.asterisk(), SCHOOL_USER.asterisk(), USER.asterisk(), SCHOOL_PERIOD.asterisk(),
        SCHOOL.asterisk()
    )
        .from(SCHOOL_USER_PERIOD)
        .leftJoin(SCHOOL_USER)
        .on(SCHOOL_USER.ID.eq(SCHOOL_USER_PERIOD.SCHOOL_USER_ID))
        .leftJoin(USER)
        .on(USER.ID.eq(SCHOOL_USER.ID))
        .leftJoin(SCHOOL_PERIOD)
        .on(SCHOOL_PERIOD.ID.eq(SCHOOL_USER_PERIOD.PERIOD_ID))
        .leftJoin(SCHOOL)
        .on(SCHOOL.ID.eq(SCHOOL_USER.SCHOOL_ID))


    fun updateUserStatus(
        userId: BigDecimal,
        status: RequestStatus,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        dsl: DSLContext = db
    ): BigDecimal {
        val schoolUserPeriodRecord =
            fetchSchoolUserPeriodRecordByUserIdSchoolAndPeriod(userId, schoolId, periodId, dsl)!!
        schoolUserPeriodRecord.apply {
            this.status = status.name
        }.update()
        return schoolUserPeriodRecord.id!!
    }

    fun fetchSchoolUserPeriodRecordByUserIdSchoolAndPeriod(
        userId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        dsl: DSLContext = db,
    ) = dsl.selectFrom(SCHOOL_USER_PERIOD).where(
        SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(
            dsl.select(SCHOOL_USER.ID).from(SCHOOL_USER).where(SCHOOL_USER.USER_ID.eq(userId))
                .and(SCHOOL_USER.SCHOOL_ID.eq(schoolId))
        ).and(SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId))
    ).fetchOne()


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

    fun changeSchoolUserPeriodStatus(
        requestValue: RequestValueJson.UserRegistration,
        requestStatus: RequestStatus,
        dsl: DSLContext
    ) {
        dsl.selectFrom(SCHOOL_USER_PERIOD)
            .where(SCHOOL_USER_PERIOD.ID.eq(requestValue.schoolUserPeriodId.toBigDecimal()))
            .fetchAny()
            ?.apply {
                status = requestStatus.name
            }?.update()
    }

}