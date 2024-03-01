package com.nevexis.backend.schoolManagement.security.user_security

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.security.SMSUserDetails
import com.nevexis.backend.schoolManagement.users.UserRegistrationInformation
import com.nevexis.backend.schoolManagement.users.UserStatus
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_PERIOD
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserSecurityService : BaseService() {

    @Autowired
    @Lazy
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var schoolUserRolesService: SchoolRolesService


    fun findActiveUserByUsername(
        username: String,
        periodId: BigDecimal? = null,
        roleId: BigDecimal? = null
    ): SMSUserDetails? =
        recordSelectOnConditionStep().where(USER.USERNAME.eq(username))
            .and(SCHOOL_USER_PERIOD.STATUS.eq(UserStatus.ACTIVE.name))
            .fetchAny()
            ?.into(UserRecord::class.java)
            ?.mapToModel(roleId, periodId)
            ?.let { SMSUserDetails(it) }

    fun createUser(user: UserRegistrationInformation, dsl: DSLContext = db): UserRecord {
        val userId = getUserSeqNextVal()
        return dsl.newRecord(USER, user).apply {
            id = userId
            password = passwordEncoder.encode(user.password)
        }.also { userRecord ->
            user.schoolRoles.groupBy { Pair(it.periodId, it.school) }.keys.map { (periodId, school) ->
                dsl.newRecord(SCHOOL_USER).apply {
                    this.id = getSchoolUserSeqNextVal()
                    this.userId = userId
                    this.schoolId = school.id
                }
            }.apply {
                dsl.batchInsert(this).execute()
            }
            userRecord.insert()
        }
    }

    fun createUserAndMapItToUserSec(user: UserRegistrationInformation, dsl: DSLContext = db) =
        createUser(user, dsl).mapToModel(dsl = dsl)


//    fun changeUserStatus(userId: BigDecimal, status: UserStatus, dsl: DSLContext = db) {
//        dsl.selectFrom(USER).where(USER.ID.eq(userId)).fetchAny()?.apply {
//            this.status = status.name
//        }?.update()
//    }


    fun UserRecord.mapToModel(
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

    fun getUserSeqNextVal(): BigDecimal =
        db.select(DSL.field("USER_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getSchoolUserSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_USER_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    private fun recordSelectOnConditionStep(dsl: DSLContext = db) =
        dsl.select(USER.asterisk(), SCHOOL_USER.asterisk(), SCHOOL_USER_PERIOD.asterisk()).from(USER)
            .leftJoin(SCHOOL_USER).on(
                SCHOOL_USER.USER_ID.eq(USER.ID)
            ).leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.ID.eq(SCHOOL_USER.ID))

}