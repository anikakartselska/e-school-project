package com.nevexis.backend.schoolManagement.requests

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.security.UserSecurityService
import com.nevexis.backend.schoolManagement.users.UserRegistrationInformation
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.`demo-project`.jooq.tables.references.REGISTRATION_REQUEST
import com.nevexis.`demo-project`.jooq.tables.references.ROLE_REQUEST
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class RequestService : BaseService() {
    @Autowired
    private lateinit var userSecurityService: UserSecurityService

    @Autowired
    private lateinit var schoolRolesService: SchoolRolesService

    fun createRegistrationRequestAndUserRoleRequests(userRegistrationInformation: UserRegistrationInformation) {
        db.transaction { transaction ->
            val newlyCreatedUser = userSecurityService.createUser(userRegistrationInformation, transaction.dsl())
            val newlyCreatedUserId = newlyCreatedUser.id!!
            createRegistrationRequest(newlyCreatedUserId, transaction.dsl())
            val schoolUserRoleIds = schoolRolesService.createSchoolRoles(
                userRegistrationInformation.schoolRoles,
                newlyCreatedUserId,
                transaction.dsl()
            )
            createRolesRequests(schoolUserRoleIds, newlyCreatedUserId, transaction.dsl())
        }
    }


    fun createRegistrationRequest(userId: BigDecimal, dsl: DSLContext) {
        dsl.newRecord(REGISTRATION_REQUEST).apply {
            id = getRegistrationRequestSeqNextVal()
            requestedByUserId = userId
            requestDate = LocalDateTime.now()
        }.insert()
    }

    fun createRolesRequests(schoolUserRoleIds: List<BigDecimal>, userId: BigDecimal, dsl: DSLContext) {
        schoolUserRoleIds.map { schoolUserRoleId ->
            dsl.newRecord(ROLE_REQUEST).apply {
                id = getRoleRequestSeqNextVal()
                requestedByUserId = userId
                this.schoolUserRoleId = schoolUserRoleId
                requestDate = LocalDateTime.now()
            }
        }
    }

    fun changeRoleRequestStatus(
        requestId: BigDecimal,
        verificationStatus: RequestStatus,
        resolvedByUserId: BigDecimal
    ) {
        getRoleRequestRecordById(requestId)?.apply {
            this.resolvedByUserId = resolvedByUserId
            this.verificationStatus = verificationStatus.name
            this.resolvedDate = LocalDateTime.now()
        }?.also { it.update() }
    }

    fun changeRegistrationRequestStatus(
        requestId: BigDecimal,
        verificationStatus: RequestStatus,
        resolvedByUserId: BigDecimal
    ) {
        getRegistrationRequestRecordById(requestId)?.apply {
            this.resolvedByUserId = resolvedByUserId
            this.verificationStatus = verificationStatus.name
            this.resolvedDate = LocalDateTime.now()
        }?.also { it.update() }
    }

    private fun getRoleRequestRecordById(requestId: BigDecimal) =
        db.selectFrom(ROLE_REQUEST).where(ROLE_REQUEST.ID.eq(requestId))
            .fetchAny()

    private fun getRegistrationRequestRecordById(requestId: BigDecimal) =
        db.selectFrom(REGISTRATION_REQUEST).where(REGISTRATION_REQUEST.ID.eq(requestId))
            .fetchAny()

    fun getRegistrationRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("REGISTRATION_REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun getRoleRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("ROLE_REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}