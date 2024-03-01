package com.nevexis.backend.schoolManagement.requests

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.security.user_security.UserSecurityService
import com.nevexis.backend.schoolManagement.users.UserRegistrationInformation
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.school_user.SchoolUserService
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class RequestService : BaseService() {
    @Autowired
    private lateinit var userService: UserSecurityService

    @Autowired
    private lateinit var schoolRolesService: SchoolRolesService

    @Autowired
    private lateinit var schoolUserService: SchoolUserService

    fun createRequestsFromUserRegistrationInformation(userRegistrationInformation: UserRegistrationInformation) {

    }

    fun RequestValueJson.mapRequestValueJsonToRequestValue(dsl: DSLContext): RequestValue {
        return when (this) {
            is RequestValueJson.UserRegistration -> RequestValue.UserRegistration(
                schoolUserService.getSchoolUserById(this.schoolUserId, dsl)
            )

            is RequestValueJson.Role -> RequestValue.Role(
                schoolRolesService.getUserSchoolRoleById(this.schoolUserRoleId, dsl)
            )
        }
    }

    fun RequestValue.mapRequestValueToRequestValueJson(dsl: DSLContext): RequestValueJson {
        return when (this) {
            is RequestValue.UserRegistration -> RequestValueJson.UserRegistration(
                this.schoolUser.id
            )

            is RequestValue.Role -> RequestValueJson.Role(
                this.schoolUserRole.id
            )
        }
    }

    fun getRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}