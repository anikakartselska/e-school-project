package com.nevexis.backend.schoolManagement.requests

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.User
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.school_user.SchoolUserService
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import com.nevexis.`demo-project`.jooq.tables.records.RequestRecord
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class RequestService : BaseService() {
    @Autowired
    private lateinit var userService: UserSecurityService

    @Autowired
    private lateinit var schoolRolesService: SchoolRolesService

    @Autowired
    private lateinit var schoolUserService: SchoolUserService

    fun createRequests(user: User) {
        db.transaction { transaction ->
            val userId = user.id?.toBigDecimal() ?: userService.createUser(user, transaction.dsl())
            schoolUserService.createSchoolUsersFromListOfSchoolUserRoles(
                userId,
                user.roles ?: emptyList(),
                transaction.dsl()
            ).map {
                RequestRecord(
                    id = getRequestSeqNextVal(),
                    requestStatus = RequestStatus.PENDING.name,
                    requestedByUserId = userId,
                    requestValue = Json.encodeToString(RequestValueJson.UserRegistration(it.toInt())),
                    requestDate = LocalDateTime.now()
                )
            }.also {
                transaction.dsl().batchInsert(it).execute()
            }

            schoolRolesService.createSchoolUserRoles(
                userId,
                user.roles ?: emptyList(),
                transaction.dsl()
            ).map {
                RequestRecord(
                    id = getRequestSeqNextVal(),
                    requestStatus = RequestStatus.PENDING.name,
                    requestedByUserId = userId,
                    requestValue = Json.encodeToString(RequestValueJson.Role(it.toInt())),
                    requestDate = LocalDateTime.now()
                )
            }.also {
                transaction.dsl().batchInsert(it).execute()
            }
        }
    }

//    fun RequestValueJson.mapRequestValueJsonToRequestValue(dsl: DSLContext): RequestValue {
//        return when (this) {
//            is RequestValueJson.UserRegistration -> RequestValue.UserRegistration(
//                schoolUserService.getSchoolUserById(this.schoolUserId, dsl)
//            )
//
//            is RequestValueJson.Role -> RequestValue.Role(
//                schoolRolesService.getUserSchoolRoleById(this.schoolUserRoleId, dsl)
//            )
//        }
//    }
//
//    fun RequestValue.mapRequestValueToRequestValueJson(dsl: DSLContext): RequestValueJson {
//        return when (this) {
//            is RequestValue.UserRegistration -> RequestValueJson.UserRegistration(
//                this.schoolUser.id
//            )
//
//            is RequestValue.Role -> RequestValueJson.Role(
//                this.schoolUserRole.id
//            )
//        }
//    }

    fun getRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}