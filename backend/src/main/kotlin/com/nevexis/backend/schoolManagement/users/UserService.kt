package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClassService
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_PERIOD
import com.nevexis.`demo-project`.jooq.tables.references.STUDENT_SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserService : BaseService() {

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var schoolUserRolesService: SchoolRolesService

    fun getAllUserViewsBySchool(schoolId: BigDecimal, periodId: BigDecimal, dsl: DSLContext = db) {
        val rolesForSchoolGroupedByUserId = schoolUserRolesService.getAllRolesFromSchoolForPeriod(schoolId, periodId)
        recordSelectOnConditionStep(dsl).where(
            SCHOOL_USER.SCHOOL_ID.eq(schoolId).and(SCHOOL_USER_PERIOD.STATUS.eq(UserStatus.ACTIVE.name))
        ).map {
            val userRecord = it.into(UserRecord::class.java)
            mapToUserView(userRecord, rolesForSchoolGroupedByUserId[userRecord.id] ?: emptyList())
        }
    }


    fun getUserByIdWithoutRoles(userId: BigDecimal, dsl: DSLContext): User? {
        return dsl.selectFrom(USER).where(USER.ID.eq(userId)).fetchAny()?.map {
            mapUserRecordToUserModel(it.into(UserRecord::class.java), emptyList())
        }
    }

    fun getAllStudentsInSchoolClass(
        schoolClassId: BigDecimal,
        periodId: BigDecimal
    ): List<StudentView> =
        db.select(USER.asterisk(), STUDENT_SCHOOL_CLASS.asterisk()).from(USER).leftJoin(STUDENT_SCHOOL_CLASS).on(
            STUDENT_SCHOOL_CLASS.STUDENT_ID.eq(USER.ID)
        ).where(
            STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId).and(
                STUDENT_SCHOOL_CLASS.PERIOD_ID.eq(periodId)
            ).and(USER.DELETED.eq("N"))
        ).orderBy(STUDENT_SCHOOL_CLASS.NUMBER_IN_CLASS)
            .fetchInto(StudentView::class.java)


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

    fun mapUserRecordToOneRoleModel(userRecord: UserRecord, schoolUserRole: SchoolUserRole): OneRoleUser {
        return OneRoleUser(
            id = userRecord.id!!,
            personalNumber = userRecord.personalNumber!!,
            email = userRecord.email!!,
            phoneNumber = userRecord.phoneNumber!!,
            firstName = userRecord.firstName!!,
            middleName = userRecord.middleName!!,
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            address = userRecord.address!!,
            role = schoolUserRole
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
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            address = userRecord.address!!,
            roles = schoolUserRoles
        )
    }

    private fun recordSelectOnConditionStep(dsl: DSLContext = db) =
        dsl.select(USER.asterisk(), SCHOOL_USER.asterisk(), SCHOOL_USER_PERIOD.asterisk()).from(USER)
            .leftJoin(SCHOOL_USER).on(
                SCHOOL_USER.USER_ID.eq(USER.ID)
            ).leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.ID.eq(SCHOOL_USER.ID))


}