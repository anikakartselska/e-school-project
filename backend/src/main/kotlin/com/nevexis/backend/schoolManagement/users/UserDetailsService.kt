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
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserDetailsService : BaseService() {

    @Autowired
    private lateinit var schoolUserRolesService: SchoolRolesService

    @Autowired
    private lateinit var schoolService: BaseService

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    fun getParentDetails(
        schoolUserRole: SchoolUserRole
    ): DetailsForUser.DetailsForParent {
        val usersChild = db.select(USER.asterisk(), PARENT_STUDENT.asterisk()).from(USER).leftJoin(PARENT_STUDENT)
            .on(USER.ID.eq(PARENT_STUDENT.STUDENT_ID))
            .where(PARENT_STUDENT.SCHOOL_USER_ROLE_ID.eq(schoolUserRole.id))
            .fetchAny()
            ?.map {
                val userRecord = it.into(UserRecord::class.java)
                val userChildRole = schoolUserRolesService.getUserRole(
                    userId = userRecord.id!!,
                    schoolId = schoolUserRole.id,
                    role = SchoolRole.STUDENT,
                    periodId = schoolUserRole.periodId
                )
                userRecord.mapToModel(userChildRole)
            } ?: error("User with ${schoolUserRole.userId} does not have PARENT rights")

        return DetailsForUser.DetailsForParent(usersChild)
    }


    fun getStudentDetails(schoolUserRole: SchoolUserRole): DetailsForUser.DetailsForStudent? =
        db.select(SCHOOL_CLASS.asterisk(), USER.asterisk(), STUDENT_SCHOOL_CLASS.NUMBER_IN_CLASS).from(
            SCHOOL_CLASS
        ).leftJoin(STUDENT_SCHOOL_CLASS).on(SCHOOL_CLASS.ID.eq(STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID))
            .leftJoin(USER).on(SCHOOL_CLASS.MAIN_TEACHER.eq(USER.ID))
            .where(STUDENT_SCHOOL_CLASS.SCHOOL_USER_ROLE_ID.eq(schoolUserRole.id))
            .fetchAny()?.map {
                DetailsForUser.DetailsForStudent(
                    schoolClassService.mapRecordToInternalModel(it),
                    it.get(DSL.field("NUMBER_IN_CLASS", BigDecimal::class.java))
                )
            }


    fun getUserDetails(
        schoolUserRole: SchoolUserRole
    ): DetailsForUser? =
        when (schoolUserRole.role) {
            SchoolRole.STUDENT -> getStudentDetails(schoolUserRole)
            SchoolRole.PARENT -> getParentDetails(schoolUserRole)
            else -> null
        }


    fun getUserWithDetailsByUserIdAndSchoolId(userId: BigDecimal, schoolUserRoleId: BigDecimal): User? {
        val schoolUserRole = schoolUserRolesService.getUserSchoolRoleById(schoolUserRoleId)
        return db.selectFrom(USER).where(USER.ID.eq(userId)).fetchAny()?.mapToModel(schoolUserRole)
    }
}