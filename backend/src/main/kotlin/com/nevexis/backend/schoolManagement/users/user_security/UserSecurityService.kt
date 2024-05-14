package com.nevexis.backend.schoolManagement.users.user_security

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.security.SMSUserDetails
import com.nevexis.backend.schoolManagement.users.OneRoleUser
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserBaseService
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class UserSecurityService : UserBaseService() {

    fun findUserByEmail(email: String) = db.selectFrom(USER).where(USER.EMAIL.eq(email))
        .fetchAny()
        ?.into(UserRecord::class.java)
        ?.mapToUserSecurityModel()

    fun updateUserPassword(newPassword: String, passwordResetToken: String) {
        val passwordResetTokenRecord =
            db.selectFrom(PASSWORD_RESET_TOKEN).where(PASSWORD_RESET_TOKEN.TOKEN.eq(passwordResetToken))
                .fetchAny()
        if (passwordResetTokenRecord?.expiryDate?.isBefore(LocalDateTime.now()) == true) {
            throw SMSError("Грешка при промяна на паролата", "Токенър за промяна на паролата е изтекъл")
        }
        db.selectFrom(USER).where(USER.ID.eq(passwordResetTokenRecord?.userId!!))
            .fetchAny()?.apply {
                password = passwordEncoder.encode(newPassword)
            }?.update()
    }

    fun changeUserPassword(userId: BigDecimal, newPassword: String, oldPassword: String) {
        val userRecord = db.selectFrom(USER).where(USER.ID.eq(userId))
            .fetchAny() ?: throw SMSError(
            "Данните не са намерени",
            "Потребителят, който търсите не съществува или е бил изтрит"
        )
        if (!passwordEncoder.matches(oldPassword, userRecord.password)) {
            throw SMSError("Грешна парола", "Старата ви парола не е правилна!")
        }
        userRecord.apply {
            password = passwordEncoder.encode(newPassword)
        }.update()
    }

    fun findUserWithAllApprovedRolesByPhoneNumber(
        phoneNumber: String
    ) = db.selectFrom(USER).where(USER.PHONE_NUMBER.eq(phoneNumber))
        .fetchAnyInto(UserRecord::class.java)?.let { userRecord ->
            val allUserRoles = schoolUserRolesService.getAllUserRoles(userRecord.id!!, RequestStatus.APPROVED)
            mapUserRecordToUserModel(userRecord, allUserRoles)
        }

    fun findUserWithAllRolesByPhoneNumberForSchoolAndPeriod(
        phoneNumber: String,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) = recordSelectOnConditionStep(db)
        .where(
            USER.PHONE_NUMBER.eq(phoneNumber).and(SCHOOL_USER.SCHOOL_ID.eq(schoolId))
                .and(SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId))
        )
        .fetchAny()?.let { record ->
            val userId = record.get(USER.ID)
            val allUserRoles = schoolUserRolesService.getAllUserRoles(userId!!)
            mapUserRecordToUserModel(record, allUserRoles)
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

    fun findActiveUserById(
        id: BigDecimal,
        periodId: BigDecimal? = null,
        roleId: BigDecimal? = null
    ): SMSUserDetails? =
        userRecordSelectOnConditionStep().where(USER.ID.eq(id))
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