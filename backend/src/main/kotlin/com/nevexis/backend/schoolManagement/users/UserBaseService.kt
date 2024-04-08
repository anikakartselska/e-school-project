package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurity
import com.nevexis.`demo-project`.jooq.tables.records.SchoolUserPeriodRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserBaseService : BaseService() {
    @Autowired
    lateinit var schoolUserRolesService: SchoolRolesService

    @Autowired
    @Lazy
    lateinit var passwordEncoder: PasswordEncoder
    fun createUser(user: User, dsl: DSLContext): BigDecimal {
        val newUserId = getUserSeqNextVal()
        dsl.newRecord(USER, user).apply {
            id = newUserId
            password = passwordEncoder.encode(user.password ?: user.phoneNumber)
            gender = user.gender.name
        }.insert()

        return newUserId
    }

    fun mapToUserView(
        record: Record,
        rolesForSchool: List<SchoolRole>
    ): UserView {
        val userRecord = record.into(UserRecord::class.java)
        val schoolUserPeriodRecord = record.into(SchoolUserPeriodRecord::class.java)
        return UserView(
            id = userRecord.id!!.toInt(),
            email = userRecord.email!!,
            firstName = userRecord.firstName!!,
            middleName = userRecord.middleName!!,
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            roles = rolesForSchool,
            status = schoolUserPeriodRecord.status?.let { RequestStatus.valueOf(it) }
        )

    }

    fun mapUserRecordToUserModel(record: Record, schoolUserRoles: List<SchoolUserRole>): User {
        val userRecord = record.into(UserRecord::class.java)
        val schoolUserPeriodRecord = record.into(SchoolUserPeriodRecord::class.java)
        return User(
            id = userRecord.id!!.toInt(),
            personalNumber = userRecord.personalNumber!!,
            email = userRecord.email!!,
            phoneNumber = userRecord.phoneNumber!!,
            firstName = userRecord.firstName!!,
            middleName = userRecord.middleName!!,
            gender = Gender.valueOf(userRecord.gender!!),
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            address = userRecord.address!!,
            roles = schoolUserRoles,
            status = schoolUserPeriodRecord.status?.let { RequestStatus.valueOf(it) }!!
        )
    }


    fun recordSelectOnConditionStep(dsl: DSLContext = db) =
        dsl.select(USER.asterisk(), SCHOOL_USER.asterisk(), SCHOOL_USER_PERIOD.asterisk())
            .from(USER)
            .leftJoin(SCHOOL_USER).on(SCHOOL_USER.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))

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
            .leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))
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
            id = id?.toInt()!!,
            email = email!!,
            firstName = firstName!!,
            middleName = middleName!!,
            lastName = lastName!!,
            username = username!!,
            role = userRole,
            password = this.password!!
        )
    }

    fun mapUserToUserView(user: User): UserView {
        return UserView(
            id = user.id!!.toInt(),
            email = user.email,
            firstName = user.firstName,
            middleName = user.middleName,
            lastName = user.lastName,
            username = user.username,
            roles = user.roles?.map { it.role } ?: emptyList(),
            status = user.status
        )
    }

    fun mapUserRecordToOneRoleModel(userRecord: UserRecord, schoolUserRole: SchoolUserRole): OneRoleUser {
        return OneRoleUser(
            id = userRecord.id!!.toInt(),
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

    fun studentRecordSelectOnConditionStep(dsl: DSLContext = db) = dsl.select(
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