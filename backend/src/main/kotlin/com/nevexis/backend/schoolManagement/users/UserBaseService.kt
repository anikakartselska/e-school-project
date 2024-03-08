package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurity
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserBaseService : BaseService() {
    @Autowired
    lateinit var schoolUserRolesService: SchoolRolesService
    fun mapToUserView(
        userRecord: UserRecord,
        rolesForSchool: List<SchoolRole>
    ): UserView {
        return UserView(
            id = userRecord.id!!,
            email = userRecord.email!!,
            firstName = userRecord.firstName!!,
            middleName = userRecord.middleName!!,
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            roles = rolesForSchool
        )

    }

    fun mapUserRecordToUserModel(userRecord: UserRecord, schoolUserRoles: List<SchoolUserRole>): User {
        return User(
            id = userRecord.id!!,
            personalNumber = userRecord.personalNumber!!,
            email = userRecord.email!!,
            phoneNumber = userRecord.phoneNumber!!,
            firstName = userRecord.firstName!!,
            middleName = userRecord.middleName!!,
            gender = Gender.valueOf(userRecord.gender!!),
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            address = userRecord.address!!,
            roles = schoolUserRoles
        )
    }


    fun recordSelectOnConditionStep(dsl: DSLContext = db) =
        dsl.select(USER.asterisk(), SCHOOL_USER.asterisk(), SCHOOL_USER_PERIOD.asterisk()).from(USER)
            .leftJoin(SCHOOL_USER).on(
                SCHOOL_USER.USER_ID.eq(USER.ID)
            ).leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.ID.eq(SCHOOL_USER.ID))

    fun recordSelectOnConditionStepJoinedWithUserRoles(dsl: DSLContext = db) =
        dsl.select(
            USER.asterisk(),
            SCHOOL_USER.asterisk(),
            SCHOOL_USER_PERIOD.asterisk(),
            SCHOOL_USER_ROLE.asterisk(),
            SCHOOL_ROLE_PERIOD.asterisk(),
            SCHOOL_PERIOD.asterisk()
        ).from(USER)
            .leftJoin(SCHOOL_USER).on(SCHOOL_USER.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.ID.eq(SCHOOL_USER.ID))
            .leftJoin(SCHOOL_USER_ROLE).on(SCHOOL_USER_ROLE.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_ROLE_PERIOD).on(SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID.eq(SCHOOL_USER_ROLE.ID))
            .leftJoin(SCHOOL_PERIOD).on(SCHOOL_USER_PERIOD.PERIOD_ID.eq(SCHOOL_PERIOD.ID))

    fun UserRecord.mapToUserSecurityModel(
        roleId: BigDecimal? = null,
        periodId: BigDecimal? = null,
        dsl: DSLContext = db
    ): UserSecurity {
        val userRole = if (roleId != null && periodId != null) {
            schoolUserRolesService.getUserSchoolRoleByIdAndPeriodId(roleId, periodId, dsl)
        } else {
            null
        }
        return UserSecurity(
            id = id!!,
            email = email!!,
            firstName = firstName!!,
            middleName = middleName!!,
            lastName = lastName!!,
            username = username!!,
            role = userRole,
            password = this.password!!
        )
    }

    fun mapUserRecordToOneRoleModel(userRecord: UserRecord, schoolUserRole: SchoolUserRole): OneRoleUser {
        return OneRoleUser(
            id = userRecord.id!!,
            personalNumber = userRecord.personalNumber!!,
            email = userRecord.email!!,
            phoneNumber = userRecord.phoneNumber!!,
            gender = Gender.valueOf(userRecord.gender!!),
            firstName = userRecord.firstName!!,
            middleName = userRecord.middleName!!,
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            address = userRecord.address!!,
            role = schoolUserRole
        )
    }

    fun getUserSeqNextVal(): BigDecimal =
        db.select(DSL.field("USER_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getSchoolUserSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun userRecordSelectOnConditionStep(dsl: DSLContext = db) =
        dsl.select(USER.asterisk(), SCHOOL_USER.asterisk(), SCHOOL_USER_PERIOD.asterisk()).from(USER)
            .leftJoin(SCHOOL_USER).on(
                SCHOOL_USER.USER_ID.eq(USER.ID)
            ).leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.ID.eq(SCHOOL_USER.ID))

    fun studentRecordSelectOnConditionStep() = db.select(
        USER.asterisk(),
        STUDENT_SCHOOL_CLASS.asterisk(),
        SCHOOL_USER.asterisk(),
        SCHOOL_USER_ROLE.asterisk(),
        SCHOOL_USER_PERIOD.asterisk(),
        SCHOOL_ROLE_PERIOD.asterisk(),
        SCHOOL_PERIOD.asterisk()
    )
        .from(STUDENT_SCHOOL_CLASS)
        .leftJoin(SCHOOL_USER_ROLE).on(SCHOOL_USER_ROLE.ID.eq(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID))
        .leftJoin(USER).on(SCHOOL_USER_ROLE.USER_ID.eq(USER.ID))
        .leftJoin(SCHOOL_USER).on(SCHOOL_USER.USER_ID.eq(USER.ID))
        .leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.ID.eq(SCHOOL_USER.ID))
        .leftJoin(SCHOOL_ROLE_PERIOD).on(SCHOOL_ROLE_PERIOD.SCHOOL_USER_ROLE_ID.eq(SCHOOL_USER_ROLE.ID))
        .leftJoin(SCHOOL_PERIOD).on(SCHOOL_PERIOD.ID.eq(SCHOOL_ROLE_PERIOD.PERIOD_ID))
}