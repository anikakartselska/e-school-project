package com.nevexis.backend.schoolManagement.users.roles

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.AdditionalRequestInformation
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.requests.RequestValueJson
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.school_period.mapSchoolPeriodRecordToSchoolPeriod
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserDetailsService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolRolePeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserRoleRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_PERIOD
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_ROLE_PERIOD
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_ROLE
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolRolesService : BaseService() {
    @Autowired
    private lateinit var schoolService: SchoolService

    @Autowired
    private lateinit var userDetailsService: UserDetailsService


    fun createSchoolUserRoles(
        userId: BigDecimal,
        schoolUserRoles: List<SchoolUserRole>,
        dsl: DSLContext
    ): List<AdditionalRequestInformation> {
        return dsl.transactionResult { transaction ->
            schoolUserRoles.map { schoolUserRole ->
                (transaction.dsl().fetchOne(
                    SCHOOL_USER_ROLE,
                    SCHOOL_USER_ROLE.ID.eq(schoolUserRole.id?.toBigDecimal())
                ) ?: transaction.dsl().newRecord(SCHOOL_USER_ROLE).apply {
                    this.schoolId = schoolUserRole.school.id.toBigDecimal()
                    this.userId = userId
                    this.role = schoolUserRole.role.name
                    id = getSchoolUserRoleSeqNextVal()
                }).let { userRoleRecord ->
                    val existingRecordFromDataBase = transaction.dsl().fetchOne(
                        SCHOOL_ROLE_PERIOD,
                        SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID.eq(userRoleRecord.id),
                        SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(schoolUserRole.period.id!!.toBigDecimal())
                    )
                    userRoleRecord to if (existingRecordFromDataBase != null) {
                        existingRecordFromDataBase to false
                    } else {
                        transaction.dsl().newRecord(SCHOOL_ROLE_PERIOD).apply {
                            this.schoolUserRoleId = userRoleRecord.id
                            this.periodId = schoolUserRole.period.id.toBigDecimal()
                            id = getSchoolUserRolePeriodSeqNextVal()
                            status = RequestStatus.PENDING.name
                        } to true
                    }
                }
            }.let { listOfPairs ->
                val schoolRoleRecords = listOfPairs.map { it.first }
                val schoolRolePeriodRecords = listOfPairs.map { it.second }
                val newlyCreatedSchoolRolePeriodRecords = listOfPairs.filter { it.second.second }
                transaction.dsl().batchStore(schoolRoleRecords).execute()
                transaction.dsl().batchStore(schoolRolePeriodRecords.map { it.first }).execute()

                schoolUserRoles.mapIndexed { index, schoolUserRole ->
                    schoolUserRole.copy(id = schoolRoleRecords[index].id?.toInt(), userId = userId.toInt())
                }.apply {
                    userDetailsService.insertUserDetailsForSchoolUserRoles(this, transaction.dsl())
                }
                newlyCreatedSchoolRolePeriodRecords.map {
                    AdditionalRequestInformation(
                        valueId = it.second.first.id!!,
                        periodId = it.second.first.periodId!!,
                        schoolId = it.first.schoolId!!
                    )
                }
            }
        }
    }

    fun getTeacherRoleId(teacherId: BigDecimal, periodId: BigDecimal, schoolId: BigDecimal) =
        db.select(SCHOOL_USER_ROLE.ID).from(SCHOOL_USER_ROLE)
            .leftJoin(SCHOOL_ROLE_PERIOD)
            .on(SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID.eq(SCHOOL_USER_ROLE.ID))
            .where(
                SCHOOL_USER_ROLE.USER_ID.eq(teacherId).and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId)).and(
                    SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId)
                ).and(SCHOOL_USER_ROLE.ROLE.eq(SchoolRole.TEACHER.name))
            ).fetchAnyInto(BigDecimal::class.java)

    fun getSchoolRolePeriodRecordByRoleId(roleId: BigDecimal, periodId: BigDecimal, dsl: DSLContext = db) =
        dsl.selectFrom(SCHOOL_ROLE_PERIOD).where(
            SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID.eq(roleId).and(
                SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId)
            )
        ).fetchAny()


    fun getUserSchoolRoleByIdAndPeriodId(id: BigDecimal, periodId: BigDecimal, dsl: DSLContext) =
        schoolRolesRecordSelectOnConditionStep(dsl)
            .where(
                SCHOOL_USER_ROLE.ID.eq(id).and(
                    SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name)
                ).and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
            ).fetchAny()?.let { mapToModel(it) }
            ?: error("User role with id: $id does not exist or is not APPROVED")

    fun getUserSchoolRoleById(id: BigDecimal, dsl: DSLContext) =
        schoolRolesRecordSelectOnConditionStep(dsl).where(
            SCHOOL_USER_ROLE.ID.eq(id)
        ).fetchAnyInto(SchoolUserRoleRecord::class.java)?.let { mapToModel(it) }
            ?: error("User role with id: $id does not exist")

    fun getAllUserRoles(userId: BigDecimal, requestStatus: RequestStatus? = null): List<SchoolUserRole> =
        schoolRolesRecordSelectOnConditionStep(db).where(
            SCHOOL_USER_ROLE.USER_ID.eq(userId)
        )
            .apply {
                if (requestStatus != null) {
                    and(
                        SCHOOL_ROLE_PERIOD.STATUS.eq(requestStatus.name)
                    )
                }
            }
            .fetch().map {
                mapToModel(it)
            }.distinct()

    fun getAllSchoolUserRolesForPeriodAndSchool(
        userId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<SchoolUserRole> =
        schoolRolesRecordSelectOnConditionStep(db).where(
            SCHOOL_USER_ROLE.USER_ID.eq(userId).and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
                .and(SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId))
        ).fetch().map {
            mapToModel(it)
        }

    fun getAllApprovedSchoolRolesForPeriodAndSchool(
        userId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<SchoolRole> =
        schoolRolesRecordSelectOnConditionStep(db).where(
            SCHOOL_USER_ROLE.USER_ID.eq(userId).and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
                .and(SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId))
                .and(
                    SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name)
                )
        ).fetch().map {
            SchoolRole.valueOf(it.get(SCHOOL_USER_ROLE.ROLE, String::class.java))
        }

    fun getAllApprovedRolesFromSchoolForPeriod(schoolId: BigDecimal, periodId: BigDecimal) =
        schoolRolesRecordSelectOnConditionStep(db)
            .where(
                SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId).and(
                    SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name)
                ).and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
            )
            .fetch()
            .map { record ->
                val userId = record.get(SCHOOL_USER_ROLE.USER_ID)!!
                val role = SchoolRole.valueOf(record.get(SCHOOL_USER_ROLE.ROLE)!!)
                Pair(userId, role)
            }
            .groupBy { it.first }
            .mapValues { (_, userIdToRoles) -> userIdToRoles.map { it.second } }

    fun getSchoolRolesByListOfSchoolRolePeriodIds(
        schoolUserPeriodIds: List<BigDecimal>,
        dsl: DSLContext
    ): Map<BigDecimal, SchoolUserRole> {
        return schoolRolesRecordSelectOnConditionStep(dsl).where(SCHOOL_ROLE_PERIOD.ID.`in`(schoolUserPeriodIds))
            .fetch()
            .associate {
                it.get(SCHOOL_ROLE_PERIOD.ID)!! to
                        mapToModel(it)
            }
    }

    fun getSchoolUserRoleSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_ROLE_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getSchoolUserRolePeriodSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_ROLE_PERIOD_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    private fun schoolRolesRecordSelectOnConditionStep(dsl: DSLContext) = dsl.select(
        SCHOOL_USER_ROLE.asterisk(),
        SCHOOL_ROLE_PERIOD.asterisk(),
        SCHOOL_PERIOD.asterisk()
    )
        .from(SCHOOL_USER_ROLE)
        .leftJoin(SCHOOL_ROLE_PERIOD)
        .on(SCHOOL_USER_ROLE.ID.eq(SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID))
        .leftJoin(SCHOOL_PERIOD)
        .on(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(SCHOOL_PERIOD.ID))

    fun mapToModel(record: Record): SchoolUserRole {
        record.into(SchoolUserRoleRecord::class.java).let {
            val schoolRolePeriodRecord = record.into(SchoolRolePeriodRecord::class.java)
            val period = record.into(SchoolPeriodRecord::class.java).mapSchoolPeriodRecordToSchoolPeriod()
            val school = schoolService.getSchoolById(it.schoolId!!)

            val schoolUserRole = SchoolUserRole(
                id = it.id!!.toInt(),
                userId = it.userId!!.toInt(),
                period = period,
                school = school,
                role = SchoolRole.valueOf(it.role!!),
                status = RequestStatus.valueOf(schoolRolePeriodRecord.status!!)
            )
            return schoolUserRole.copy(
                detailsForUser = userDetailsService.getUserDetailsPerSchoolUserRole(
                    schoolUserRole,
                    period.id!!.toBigDecimal(),
                    school.id.toBigDecimal()
                )
            )
        }
    }

    fun changeSchoolUserRolePeriodStatus(
        requestValue: RequestValueJson.Role,
        requestStatus: RequestStatus,
        dsl: DSLContext
    ) {
        dsl.selectFrom(SCHOOL_ROLE_PERIOD)
            .where(SCHOOL_ROLE_PERIOD.ID.eq(requestValue.schoolUserRolePeriodId.toBigDecimal()))
            .fetchAny()
            ?.apply {
                status = requestStatus.name
            }?.update()
    }
}