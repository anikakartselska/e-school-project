package com.nevexis.backend.schoolManagement.security

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.UserRegistrationInformation
import com.nevexis.backend.schoolManagement.users.UserStatus
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
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

    fun findUserByUsername(username: String, roleId: BigDecimal? = null) =
        db.selectFrom(USER).where(USER.USERNAME.eq(username)).fetchAny()?.mapToModel(roleId)
            ?.let { SMSUserDetails(it) }

    fun createUser(user: UserRegistrationInformation, dsl: DSLContext = db): UserRecord {
        val userId = getUserSeqNextVal()
        return dsl.newRecord(USER, user).apply {
            id = userId
            password = passwordEncoder.encode(user.password)
            status = UserStatus.CREATED.name
        }.also { it.insert() }
    }

    fun createUserAndMapItToUserSec(user: UserRegistrationInformation, dsl: DSLContext = db) =
        createUser(user, db).mapToModel()


    fun changeUserStatus(userId: BigDecimal, status: UserStatus, dsl: DSLContext = db) {
        dsl.selectFrom(USER).where(USER.ID.eq(userId)).fetchAny()?.apply {
            this.status = status.name
        }?.update()
    }


    fun UserRecord.mapToModel(roleId: BigDecimal? = null): UserSec {
        val userRoles = if (roleId != null) {
            schoolUserRolesService.getUserSchoolRoleById(roleId)
        } else {
            null
        }
        return UserSec(
            id = id!!,
            email = email!!,
            firstName = firstName!!,
            middleName = middleName!!,
            lastName = lastName!!,
            username = username!!,
            roles = userRoles,
            password = this.password!!
        )
    }

    fun getUserSeqNextVal(): BigDecimal =
        db.select(DSL.field("USER_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }


}