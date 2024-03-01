package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.schoolClass.SchoolClassService
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.PARENT_STUDENT
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.STUDENT_SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserDetailsService : BaseService() {

    @Autowired
    @Lazy
    private lateinit var schoolUserRolesService: SchoolRolesService


    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    @Lazy
    private lateinit var userService: UserService

    fun getParentDetailsPerSchoolUserRoles(
        schoolUserRoles: List<SchoolUserRole>
    ): List<DetailsForUser.DetailsForParent> {
        val schoolId = schoolUserRoles.firstOrNull()?.school?.id ?: error("School user role was null")
        val periodId = schoolUserRoles.firstOrNull()?.periodId ?: error("School user role was null")

        return db.select(USER.asterisk(), PARENT_STUDENT.asterisk()).from(USER).leftJoin(PARENT_STUDENT)
            .on(USER.ID.eq(PARENT_STUDENT.STUDENT_ID))
            .where(PARENT_STUDENT.SCHOOL_USER_ROLE_ID.`in`(schoolUserRoles.map { it.id }))
            .fetch()
            .map {
                val userRecord = it.into(UserRecord::class.java)
                val userChildRole = schoolUserRolesService.getUserRole(
                    userId = userRecord.id!!,
                    schoolId = schoolId,
                    role = SchoolRole.STUDENT,
                    periodId = periodId
                )
                val usersChild = userService.mapUserRecordToOneRoleModel(userRecord, userChildRole)
                DetailsForUser.DetailsForParent(usersChild)
            }
    }


    fun getStudentDetailsPerSchoolUserRoles(schoolUserRoles: List<SchoolUserRole>): List<DetailsForUser.DetailsForStudent> =
        db.select(SCHOOL_CLASS.asterisk(), USER.asterisk(), STUDENT_SCHOOL_CLASS.NUMBER_IN_CLASS).from(
            SCHOOL_CLASS
        ).leftJoin(STUDENT_SCHOOL_CLASS).on(SCHOOL_CLASS.ID.eq(STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID))
            .leftJoin(USER).on(SCHOOL_CLASS.MAIN_TEACHER.eq(USER.ID))
            .where(STUDENT_SCHOOL_CLASS.SCHOOL_USER_ROLE_ID.`in`(schoolUserRoles.map { it.id }))
            .fetch().map {
                DetailsForUser.DetailsForStudent(
                    schoolClassService.mapRecordToInternalModel(it),
                    it.get(DSL.field("NUMBER_IN_CLASS", BigDecimal::class.java))
                )
            }


    fun getUserDetailsPerSchoolUserRole(
        schoolUserRole: SchoolUserRole
    ): DetailsForUser? =
        when (schoolUserRole.role) {
            SchoolRole.STUDENT -> getStudentDetailsPerSchoolUserRoles(listOf(schoolUserRole)).firstOrNull()
            SchoolRole.PARENT -> getParentDetailsPerSchoolUserRoles(listOf(schoolUserRole)).firstOrNull()
            else -> null
        }

    fun getUserDetailsPerListOfSchoolUserRoles(
        schoolUserRoles: List<SchoolUserRole>
    ): List<DetailsForUser> = schoolUserRoles.groupBy { it.role }.mapNotNull { (role, schoolUserRoles) ->
        when (role) {
            SchoolRole.STUDENT -> getStudentDetailsPerSchoolUserRoles(schoolUserRoles)
            SchoolRole.PARENT -> getParentDetailsPerSchoolUserRoles(schoolUserRoles)
            else -> null
        }
    }.flatten()


//    fun getUserWithDetailsByUserIdAndSchoolId(userId: BigDecimal, schoolUserRoleId: BigDecimal): LoggedInUserView? {
//        val schoolUserRole = schoolUserRolesService.getUserSchoolRoleById(schoolUserRoleId)
//        return db.selectFrom(USER).where(USER.ID.eq(userId)).fetchAny()
//            ?.let { userRecord -> userService.mapUserRecordToModel(userRecord, schoolUserRole) }
//    }
}