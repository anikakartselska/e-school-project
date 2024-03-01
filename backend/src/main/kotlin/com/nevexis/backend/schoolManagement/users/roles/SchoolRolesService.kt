package com.nevexis.backend.schoolManagement.users.roles

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserDetailsService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserRoleRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_ROLE
import org.jooq.DSLContext
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

    fun getAllUserRolesBySchoolId(userId: BigDecimal, schoolId: BigDecimal) =
        db.select(SCHOOL_USER_ROLE.ROLE).from(SCHOOL_USER_ROLE).where(
            SCHOOL_USER_ROLE.USER_ID.eq(userId).and(
                SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId).and(
                    SCHOOL_USER_ROLE.STATUS.eq("APPROVED")
                )
            )
        ).fetchInto(String::class.java).map { role -> SchoolRole.valueOf(role) }

    fun getApprovedUserSchoolRoleById(id: BigDecimal, dsl: DSLContext) = dsl.selectFrom(SCHOOL_USER_ROLE).where(
        SCHOOL_USER_ROLE.ID.eq(id).and(
            SCHOOL_USER_ROLE.STATUS.eq("APPROVED")
        )
    ).fetchAnyInto(SchoolUserRoleRecord::class.java)?.mapToModel()
        ?: error("User role with id: $id does not exist or is not APPROVED")

    fun getUserSchoolRoleById(id: BigDecimal, dsl: DSLContext) = dsl.selectFrom(SCHOOL_USER_ROLE).where(
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
            return db.selectFrom(this).where(
                USER_ID.eq(userId).and(SCHOOL_ID.eq(schoolId)).and(ROLE.eq(role.name))
                    .and(STATUS.eq(RequestStatus.APPROVED.name))
                    .and(PERIOD_ID.eq(periodId))
            ).fetchAny()?.mapToModel() ?: error("User with id $userId is not a ${role.name}")
        }
    }

    fun getAllUserRoles(userId: BigDecimal): List<SchoolUserRole> =
        db.selectFrom(SCHOOL_USER_ROLE).where(
            SCHOOL_USER_ROLE.USER_ID.eq(userId).and(
                SCHOOL_USER_ROLE.STATUS.eq("APPROVED")
            )
        ).fetch().map { it.mapToModel() }

    fun getAllRolesFromSchool(schoolId: BigDecimal) =
        db.selectFrom(SCHOOL_USER_ROLE)
            .where(
                SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId).and(
                    SCHOOL_USER_ROLE.STATUS.eq("APPROVED")
                )
            )
            .fetch()
            .map { record ->
                val userId = record.get(SCHOOL_USER_ROLE.USER_ID)!!
                val role = SchoolRole.valueOf(record.get(SCHOOL_USER_ROLE.ROLE)!!)
                Pair(userId, role)
            }
            .groupBy { it.first }
            .mapValues { (_, userIdToRoles) -> userIdToRoles.map { it.second } }


    fun createSchoolRoles(
        schoolUserRoles: List<SchoolUserRole>,
        userId: BigDecimal,
        dsl: DSLContext
    ): List<BigDecimal> {
        return schoolUserRoles.map { schoolUserRole ->
            val newId = getSchoolUserRoleSeqNextVal()
            dsl.newRecord(SCHOOL_USER_ROLE, schoolUserRole).apply {
                this.id = newId
                this.userId = userId
                this.role = schoolUserRole.role.name
                this.schoolId = schoolUserRole.school.id
            }
        }.apply {
            dsl.batchInsert(this).execute()
        }.let { schoolUserRoleRecords -> schoolUserRoleRecords.map { it.id!! } }
    }

    fun changeSchoolRolesStatus(
        schoolUserRoleIdsToStatusMap: Map<BigDecimal, RequestStatus>,
        dsl: DSLContext
    ) {
        dsl.selectFrom(SCHOOL_USER_ROLE).where(SCHOOL_USER_ROLE.ID.`in`(schoolUserRoleIdsToStatusMap.keys))
            .fetch()
            .map {
                it.apply {
                    this.status = schoolUserRoleIdsToStatusMap[id!!]?.name
                }
            }.also {
                dsl.batchUpdate(it)
            }
    }


    fun getSchoolUserRoleSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_ROLE_REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun SchoolUserRoleRecord.mapToModel(): SchoolUserRole {
        val schoolUserRole = SchoolUserRole(
            id = this.id!!,
            userId = this.userId!!,
            school = schoolService.getSchoolById(this.schoolId!!),
            periodId = this.periodId!!,
            role = SchoolRole.valueOf(this.role!!),
            status = RequestStatus.valueOf(this.status!!)
        )
        return schoolUserRole.copy(detailsForUser = userDetailsService.getUserDetailsPerSchoolUserRole(schoolUserRole))
    }
}