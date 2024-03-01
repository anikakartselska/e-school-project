package com.nevexis.backend.schoolManagement.users.roles

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserDetailsService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolRolePeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserRoleRecord
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

//    fun getAllUserRolesBySchoolId(userId: BigDecimal, schoolId: BigDecimal) =
//        db.select(SCHOOL_USER_ROLE.ROLE).from(SCHOOL_USER_ROLE).where(
//            SCHOOL_USER_ROLE.USER_ID.eq(userId).and(
//                SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId).and(
//                    SCHOOL_USER_ROLE.STATUS.eq("APPROVED")
//                )
//            )
//        ).fetchInto(String::class.java).map { role -> SchoolRole.valueOf(role) }

    fun getUserSchoolRoleByIdAndPeriodId(id: BigDecimal, periodId: BigDecimal, dsl: DSLContext) =
        recordSelectOnConditionStep(dsl)
            .where(
                SCHOOL_USER_ROLE.ID.eq(id).and(
                    SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name)
                ).and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
            ).fetchAny()?.mapToModel()
            ?: error("User role with id: $id does not exist or is not APPROVED")

    fun getUserSchoolRoleById(id: BigDecimal, dsl: DSLContext) =
        recordSelectOnConditionStep(dsl).where(
            SCHOOL_USER_ROLE.ID.eq(id)
        ).fetchAnyInto(SchoolUserRoleRecord::class.java)?.mapToModel()
            ?: error("User role with id: $id does not exist")

    fun getUserRole(
        userId: BigDecimal,
        schoolId: BigDecimal,
        role: SchoolRole,
        periodId: BigDecimal
    ): SchoolUserRole {
        with(SCHOOL_USER_ROLE) {
            return recordSelectOnConditionStep(db).where(
                USER_ID.eq(userId).and(SCHOOL_ID.eq(schoolId)).and(ROLE.eq(role.name))
                    .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                    .and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
            ).fetchAny()?.mapToModel() ?: error("User with id $userId is not a ${role.name}")
        }
    }

    fun getAllUserRoles(userId: BigDecimal): List<SchoolUserRole> =
        recordSelectOnConditionStep(db).where(
            SCHOOL_USER_ROLE.USER_ID.eq(userId).and(
                SCHOOL_ROLE_PERIOD.STATUS.eq("APPROVED")
            )
        ).fetch().map { it.mapToModel() }

    fun getAllRolesFromSchoolForPeriod(schoolId: BigDecimal, periodId: BigDecimal) =
        recordSelectOnConditionStep(db)
            .where(
                SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId).and(
                    SCHOOL_ROLE_PERIOD.STATUS.eq("APPROVED")
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


//    fun createSchoolRoles(
//        schoolUserRoles: List<SchoolUserRole>,
//        userId: BigDecimal,
//        dsl: DSLContext
//    ): List<BigDecimal> {
//        return schoolUserRoles.map { schoolUserRole ->
//            val newId = getSchoolUserRoleSeqNextVal()
//            dsl.newRecord(SCHOOL_USER_ROLE, schoolUserRole).apply {
//                this.id = newId
//                this.userId = userId
//                this.role = schoolUserRole.role.name
//                this.schoolId = schoolUserRole.school.id
//            }
//        }.apply {
//            dsl.batchInsert(this).execute()
//        }.let { schoolUserRoleRecords -> schoolUserRoleRecords.map { it.id!! } }
//    }


    fun getSchoolUserRoleSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_ROLE_REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    private fun recordSelectOnConditionStep(dsl: DSLContext) = dsl.select(
        SCHOOL_USER_ROLE.asterisk(),
        SCHOOL_ROLE_PERIOD.asterisk()
    )
        .from(SCHOOL_USER_ROLE)
        .leftJoin(SCHOOL_ROLE_PERIOD)
        .on(SCHOOL_USER_ROLE.ID.eq(SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID))

    fun Record.mapToModel(): SchoolUserRole {
        this.into(SchoolUserRoleRecord::class.java).let {
            val approvedSchoolRolePeriodRecord = this.into(SchoolRolePeriodRecord::class.java)
            val schoolUserRole = SchoolUserRole(
                id = it.id!!,
                userId = it.userId!!,
                school = schoolService.getSchoolById(it.schoolId!!),
                periodId = approvedSchoolRolePeriodRecord.periodId!!,
                role = SchoolRole.valueOf(it.role!!),
                status = RequestStatus.valueOf(approvedSchoolRolePeriodRecord.status!!)
            )
            return schoolUserRole.copy(
                detailsForUser = userDetailsService.getUserDetailsPerSchoolUserRole(
                    schoolUserRole
                )
            )
        }
    }
}