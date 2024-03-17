package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClassService
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.`demo-project`.jooq.tables.records.ParentStudentRecord
import com.nevexis.`demo-project`.jooq.tables.records.StudentSchoolClassRecord
import com.nevexis.`demo-project`.jooq.tables.references.PARENT_STUDENT
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.STUDENT_SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserDetailsService : BaseService() {

    @Autowired
    @Lazy
    private lateinit var schoolUserRolesService: SchoolRolesService


    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    @Lazy
    private lateinit var userService: UserService

    fun insertUserDetailsForSchoolUserRoles(
        schoolUserRoles: List<SchoolUserRole>,
        dsl: DSLContext
    ) {
        schoolUserRoles.mapNotNull {
            when (it.role) {
                SchoolRole.ADMIN, SchoolRole.TEACHER -> null
                SchoolRole.STUDENT -> createStudentClassRecordForSchoolUserRole(it, dsl)
                SchoolRole.PARENT -> createParentStudentRecordForSchoolUserRole(it, dsl)
            }
        }.apply { dsl.batchStore(this).execute() }
    }

    private fun createStudentClassRecordForSchoolUserRole(
        schoolUserRole: SchoolUserRole,
        dsl: DSLContext
    ): StudentSchoolClassRecord {
        if (schoolUserRole.detailsForUser !is DetailsForUser.DetailsForStudent) {
            error("User with ${schoolUserRole.userId} does not have student details")
        }
        return dsl.selectFrom(STUDENT_SCHOOL_CLASS).where(
            STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID
                .eq(schoolUserRole.id?.toBigDecimal())
        )
            .fetchOne() ?: dsl.newRecord(STUDENT_SCHOOL_CLASS).apply {
            schoolClassId = schoolUserRole.detailsForUser.schoolClass.id.toBigDecimal()
            numberInClass = schoolUserRole.detailsForUser.numberInClass?.toBigDecimal()
            studentSchoolUserRoleId = schoolUserRole.id?.toBigDecimal()
        }
    }

    private fun createParentStudentRecordForSchoolUserRole(
        schoolUserRole: SchoolUserRole,
        dsl: DSLContext
    ): ParentStudentRecord {
        if (schoolUserRole.detailsForUser !is DetailsForUser.DetailsForParent) {
            error("User with ${schoolUserRole.userId} does not have parent details")
        }
        return dsl.selectFrom(PARENT_STUDENT)
            .where(PARENT_STUDENT.PARENT_SCHOOL_USER_ROLE_ID.eq(schoolUserRole.id?.toBigDecimal()))
            .fetchOne() ?: dsl.newRecord(PARENT_STUDENT).apply {
            parentSchoolUserRoleId = schoolUserRole.id?.toBigDecimal()
            studentSchoolUserRoleId = schoolUserRole.detailsForUser.child.role.id?.toBigDecimal()
        }
    }

    fun getParentDetailsPerSchoolUserRolesAndPeriodId(
        schoolUserRoles: List<SchoolUserRole>,
        periodId: BigDecimal
    ): List<DetailsForUser.DetailsForParent> {
        val schoolUserRoleIds = schoolUserRoles.map { it.id }
        return db.select(PARENT_STUDENT.STUDENT_SCHOOL_USER_ROLE_ID).from(PARENT_STUDENT).where(
            PARENT_STUDENT.PARENT_SCHOOL_USER_ROLE_ID.`in`(schoolUserRoleIds)
        ).fetchInto(BigDecimal::class.java)
            .let { roleIds ->
                userService.findUsersByItsRoleIdsAndPeriodId(roleIds, periodId).map {
                    DetailsForUser.DetailsForParent(it)
                }
            }
    }


    fun getStudentDetailsPerSchoolUserRolesAndPeriodId(
        schoolUserRoles: List<SchoolUserRole>,
        periodId: BigDecimal
    ): List<DetailsForUser.DetailsForStudent> =
        db.select(
            SCHOOL_CLASS.asterisk(),
            USER.asterisk(),
            STUDENT_SCHOOL_CLASS.NUMBER_IN_CLASS
        ).from(
            SCHOOL_CLASS
        ).leftJoin(STUDENT_SCHOOL_CLASS).on(SCHOOL_CLASS.ID.eq(STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID))
            .leftJoin(USER)
            .on(SCHOOL_CLASS.MAIN_TEACHER.eq(USER.ID)) //TODO main teacher id should be connected to SCHOOL_USER_ROLE
            .where(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID.`in`(schoolUserRoles.map { it.id }))
            .and(SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(periodId))
            .fetch().map {
                DetailsForUser.DetailsForStudent(
                    schoolClassService.mapRecordToInternalModel(it),
                    it.get(DSL.field("NUMBER_IN_CLASS", BigDecimal::class.java))?.toInt()
                )
            }


    fun getUserDetailsPerSchoolUserRole(
        schoolUserRole: SchoolUserRole,
        periodId: BigDecimal
    ): DetailsForUser? =
        when (schoolUserRole.role) {
            SchoolRole.STUDENT -> getStudentDetailsPerSchoolUserRolesAndPeriodId(
                listOf(schoolUserRole),
                periodId
            ).firstOrNull()

            SchoolRole.PARENT -> getParentDetailsPerSchoolUserRolesAndPeriodId(
                listOf(schoolUserRole),
                periodId
            ).firstOrNull()

            else -> null
        }

    fun getUserDetailsPerListOfSchoolUserRoles(
        schoolUserRoles: List<SchoolUserRole>,
        periodId: BigDecimal
    ): List<DetailsForUser> = schoolUserRoles.groupBy { it.role }.mapNotNull { (role, schoolUserRoles) ->
        when (role) {
            SchoolRole.STUDENT -> getStudentDetailsPerSchoolUserRolesAndPeriodId(schoolUserRoles, periodId)
            SchoolRole.PARENT -> getParentDetailsPerSchoolUserRolesAndPeriodId(schoolUserRoles, periodId)
            else -> null
        }
    }.flatten()


}