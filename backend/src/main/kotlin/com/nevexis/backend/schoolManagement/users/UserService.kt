package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClassService
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_USER_ROLE
import com.nevexis.`demo-project`.jooq.tables.references.STUDENT_SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserService : BaseService() {

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var schoolUserRolesService: SchoolRolesService

    fun getAllUserViewsBySchool(schoolId: BigDecimal): List<UserView> =
        db.select(USER.asterisk(), SCHOOL_USER_ROLE.SCHOOL_ID).from(USER).leftJoin(SCHOOL_USER_ROLE).on(
            SCHOOL_USER_ROLE.USER_ID.eq(USER.ID)
        ).where(SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId).and(USER.DELETED.eq("N"))).distinct()
            .map { mapToUserView(it) }

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


    fun mapToUserView(record: Record): UserView {
        record.into(UserRecord::class.java).let { userRecord ->
            return UserView(
                id = userRecord.id!!,
                email = userRecord.email!!,
                firstName = userRecord.firstName!!,
                middleName = userRecord.middleName!!,
                lastName = userRecord.lastName!!,
                username = userRecord.username!!
            )
        }

    }

    fun mapUserRecordToModel(userRecord: UserRecord,schoolUserRole: SchoolUserRole): User {
        return User(
            id = userRecord.id!!,
            personalNumber = userRecord.personalNumber!!,
            email = userRecord.email!!,
            phoneNumber = userRecord.phoneNumber,
            firstName = userRecord.firstName!!,
            middleName = userRecord.middleName!!,
            lastName = userRecord.lastName!!,
            username = userRecord.username!!,
            address = userRecord.address!!,
            role = schoolUserRole
        )
    }

}