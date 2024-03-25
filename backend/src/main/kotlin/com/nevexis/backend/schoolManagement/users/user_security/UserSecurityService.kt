package com.nevexis.backend.schoolManagement.users.user_security

import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.security.SMSUserDetails
import com.nevexis.backend.schoolManagement.users.OneRoleUser
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserBaseService
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserSecurityService : UserBaseService() {

    fun findUserWithAllApprovedRolesByPhoneNumber(
        phoneNumber: String
    ) = db.selectFrom(USER).where(USER.PHONE_NUMBER.eq(phoneNumber))
        .fetchAnyInto(UserRecord::class.java)?.let { userRecord ->
            val allUserRoles = schoolUserRolesService.getAllUserRoles(userRecord.id!!, RequestStatus.APPROVED)
            mapUserRecordToUserModel(userRecord, allUserRoles)
        }

    fun findUserWithAllRolesByPhoneNumber(
        phoneNumber: String
    ) = db.selectFrom(USER).where(USER.PHONE_NUMBER.eq(phoneNumber))
        .fetchAnyInto(UserRecord::class.java)?.let { userRecord ->
            val allUserRoles = schoolUserRolesService.getAllUserRoles(userRecord.id!!, RequestStatus.APPROVED)
            mapUserRecordToUserModel(userRecord, allUserRoles)
        }


    fun findActiveUserByUsername(
        username: String,
        periodId: BigDecimal? = null,
        roleId: BigDecimal? = null
    ): SMSUserDetails? =
        userRecordSelectOnConditionStep().where(USER.USERNAME.eq(username))
            .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
            .fetchAny()
            ?.into(UserRecord::class.java)
            ?.mapToUserSecurityModel(roleId, periodId)
            ?.let { SMSUserDetails(it) }

    fun findStudentByPhoneNumberPeriodAndSchoolClass(
        phoneNumber: String,
        periodId: BigDecimal,
        schoolClassId: BigDecimal
    ): OneRoleUser = studentRecordSelectOnConditionStep()
        .where(
            USER.PHONE_NUMBER.eq(phoneNumber)
                .and(
                    STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId)
                ).and(
                    SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
                ).and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                .and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
                .and(SCHOOL_USER_ROLE.ROLE.eq(SchoolRole.STUDENT.name))
        ).fetchAny()?.map {
            val schoolUserRole = schoolUserRolesService.mapToModel(it)
            mapUserRecordToOneRoleModel(it.into(UserRecord::class.java), schoolUserRole)
        } ?: error("Student with phone number $phoneNumber does not exist in the selected period and school class")


}