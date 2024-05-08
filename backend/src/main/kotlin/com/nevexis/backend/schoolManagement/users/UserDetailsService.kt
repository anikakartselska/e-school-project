package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.subject.SubjectService
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.`demo-project`.jooq.tables.records.ParentStudentRecord
import com.nevexis.`demo-project`.jooq.tables.records.StudentSchoolClassRecord
import com.nevexis.`demo-project`.jooq.tables.records.TeacherQualifiedSubjectRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserDetailsService : BaseService() {

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    @Lazy
    private lateinit var userService: UserService

    @Autowired
    @Lazy
    private lateinit var subjectService: SubjectService

    companion object {
        private val STUDENT_USER = USER.`as`("Student")
        private val STUDENT_USER_ROLE = SCHOOL_USER_ROLE.`as`("StudentUserRole")
        private val STUDENT_ROLE_PERIOD = SCHOOL_ROLE_PERIOD.`as`("StudentRolePeriod")
    }

    fun insertUserDetailsForSchoolUserRoles(
        schoolUserRoles: List<SchoolUserRole>,
        dsl: DSLContext
    ) {
        schoolUserRoles.mapNotNull {
            when (it.role) {
                SchoolRole.ADMIN -> null
                SchoolRole.STUDENT -> listOf(createStudentClassRecordForSchoolUserRole(it, dsl))
                SchoolRole.PARENT -> listOf(createParentStudentRecordForSchoolUserRole(it, dsl))
                SchoolRole.TEACHER -> createTeacherQualifiedSubjectRecordForSchoolUserRole(it, dsl)
            }
        }.apply {
            dsl.batchStore(this.flatten()).execute()
        }
    }

    private fun createStudentClassRecordForSchoolUserRole(
        schoolUserRole: SchoolUserRole,
        dsl: DSLContext
    ): StudentSchoolClassRecord {
        if (schoolUserRole.detailsForUser !is DetailsForUser.DetailsForStudent) {
            error("User with ${schoolUserRole.userId} does not have student details")
        }
        return dsl.selectFrom(STUDENT_SCHOOL_CLASS).where(
            STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID
                .eq(schoolUserRole.id?.toBigDecimal())
                .and(STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolUserRole.detailsForUser.schoolClass.id!!.toBigDecimal()))
        ).fetchOne() ?: dsl.newRecord(STUDENT_SCHOOL_CLASS).apply {
            id = getStudentSchoolClassSeqNextVal()
            schoolClassId = schoolUserRole.detailsForUser.schoolClass.id.toBigDecimal()
            studentSchoolUserRoleId = schoolUserRole.id?.toBigDecimal()
            numberInClass = schoolUserRole.detailsForUser.numberInClass?.toBigDecimal()
        }
    }

    private fun createParentStudentRecordForSchoolUserRole(
        schoolUserRole: SchoolUserRole,
        dsl: DSLContext
    ): ParentStudentRecord {
        if (schoolUserRole.detailsForUser !is DetailsForUser.DetailsForParent) {
            error("User with ${schoolUserRole.userId} does not have parent details")
        }
        return dsl.selectFrom(PARENT_STUDENT)
            .where(PARENT_STUDENT.PARENT_SCHOOL_USER_ROLE_ID.eq(schoolUserRole.id?.toBigDecimal()))
            .fetchOne() ?: dsl.newRecord(PARENT_STUDENT).apply {
            parentSchoolUserRoleId = schoolUserRole.id?.toBigDecimal()
            studentSchoolUserRoleId = schoolUserRole.detailsForUser.child.role.id?.toBigDecimal()
        }
    }


    private fun createTeacherQualifiedSubjectRecordForSchoolUserRole(
        schoolUserRole: SchoolUserRole,
        dsl: DSLContext
    ): List<TeacherQualifiedSubjectRecord> {
        if (schoolUserRole.detailsForUser !is DetailsForUser.DetailsForTeacher) {
            error("User with ${schoolUserRole.userId} does not have teacher details")
        }
        return dsl.selectFrom(TEACHER_QUALIFIED_SUBJECT).where(
            TEACHER_QUALIFIED_SUBJECT.TEACHER_ID.eq(schoolUserRole.userId!!.toBigDecimal())
                .and(TEACHER_QUALIFIED_SUBJECT.SUBJECT_NAME.`in`(schoolUserRole.detailsForUser.qualifiedSubjects))
        ).fetch().plus(schoolUserRole.detailsForUser.qualifiedSubjects.map { subject ->
            dsl.newRecord(TEACHER_QUALIFIED_SUBJECT).apply {
                subjectName = subject
                teacherId = schoolUserRole.userId.toBigDecimal()
            }
        }).distinctBy { it.subjectName }

    }


    fun getParentDetailsPerSchoolUserRolesAndPeriodId(
        schoolUserRoles: List<SchoolUserRole>,
        periodId: BigDecimal
    ): List<DetailsForUser.DetailsForParent> {
        val schoolUserRoleIds = schoolUserRoles.map { it.id }
        return db.select(PARENT_STUDENT.STUDENT_SCHOOL_USER_ROLE_ID).from(PARENT_STUDENT).where(
            PARENT_STUDENT.PARENT_SCHOOL_USER_ROLE_ID.`in`(schoolUserRoleIds)
        ).fetchInto(BigDecimal::class.java)
            .let { roleIds ->
                userService.findApprovedUsersByItsRoleIdsAndPeriodId(roleIds, periodId).map {
                    DetailsForUser.DetailsForParent(it)
                }
            }
    }

    fun getStudentDetailsPerSchoolUserRolesAndPeriodId(
        schoolUserRoles: List<SchoolUserRole>,
        periodId: BigDecimal
    ): List<DetailsForUser.DetailsForStudent> =
        db.select(
            SCHOOL_CLASS.asterisk(),
            USER.asterisk(),
            SCHOOL_USER.asterisk(),
            SCHOOL_USER_PERIOD.asterisk(),
            SCHOOL_USER_ROLE.asterisk(),
            STUDENT_SCHOOL_CLASS.asterisk(),
            STUDENT_USER_ROLE.USER_ID,
            STUDENT_SCHOOL_CLASS.ID,
            STUDENT_SCHOOL_CLASS.NUMBER_IN_CLASS
        ).from(
            SCHOOL_CLASS
        ).leftJoin(STUDENT_SCHOOL_CLASS).on(SCHOOL_CLASS.ID.eq(STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID))
            .leftJoin(SCHOOL_USER_ROLE)
            .on(SCHOOL_USER_ROLE.ID.eq(SCHOOL_CLASS.MAIN_TEACHER_ROLE_ID))
            .leftJoin(USER)
            .on(SCHOOL_USER_ROLE.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER)
            .on(SCHOOL_USER.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER_PERIOD)
            .on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))
            .leftJoin(STUDENT_USER_ROLE).on(STUDENT_USER_ROLE.ID.eq(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID))
            .where(STUDENT_SCHOOL_CLASS.STUDENT_SCHOOL_USER_ROLE_ID.`in`(schoolUserRoles.map { it.id }))
            .and(SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(periodId))
            .fetch().map {
                DetailsForUser.DetailsForStudent(
                    schoolClassService.mapRecordToInternalModel(it),
                    it.get(DSL.field("NUMBER_IN_CLASS", BigDecimal::class.java))?.toInt(),
                    userService.getStudentsParents(
                        it.get(STUDENT_USER_ROLE.USER_ID, BigDecimal::class.java),
                        periodId,
                        it.get(SCHOOL_USER.SCHOOL_ID, BigDecimal::class.java)
                    )
                )
            }

    fun getTeacherDetails(
        teacherIds: List<BigDecimal>,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): List<DetailsForUser.DetailsForTeacher> {
        if (teacherIds.isEmpty()) {
            return emptyList()
        }
        return db.selectFrom(TEACHER_QUALIFIED_SUBJECT).where(TEACHER_QUALIFIED_SUBJECT.TEACHER_ID.`in`(teacherIds))
            .fetch().groupBy { it.teacherId }.map { (teacherId, recordsForCurrentTeacher) ->
                DetailsForUser.DetailsForTeacher(
                    recordsForCurrentTeacher.map { it.subjectName!! },
                    subjectService.getAllSubjectsTaughtByTeacher(teacherId!!, periodId, schoolId)
                )
            }
    }


    fun getUserDetailsPerSchoolUserRole(
        schoolUserRole: SchoolUserRole,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): DetailsForUser? =
        when (schoolUserRole.role) {
            SchoolRole.STUDENT -> getStudentDetailsPerSchoolUserRolesAndPeriodId(
                listOf(schoolUserRole),
                periodId
            ).firstOrNull()

            SchoolRole.PARENT -> getParentDetailsPerSchoolUserRolesAndPeriodId(
                listOf(schoolUserRole),
                periodId
            ).firstOrNull()

            SchoolRole.TEACHER -> getTeacherDetails(
                listOf(schoolUserRole.userId!!.toBigDecimal()),
                periodId,
                schoolId
            ).firstOrNull()

            else -> null
        }

    fun getUserDetailsPerListOfSchoolUserRoles(
        schoolUserRoles: List<SchoolUserRole>,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): List<DetailsForUser> = schoolUserRoles.groupBy { it.role }.mapNotNull { (role, schoolUserRoles) ->
        when (role) {
            SchoolRole.STUDENT -> getStudentDetailsPerSchoolUserRolesAndPeriodId(schoolUserRoles, periodId)
            SchoolRole.PARENT -> getParentDetailsPerSchoolUserRolesAndPeriodId(schoolUserRoles, periodId)
            SchoolRole.TEACHER -> getTeacherDetails(
                schoolUserRoles.map { it.userId!!.toBigDecimal() },
                periodId,
                schoolId
            )

            else -> null
        }
    }.flatten()

    fun getStudentSchoolClassSeqNextVal(): BigDecimal =
        db.select(DSL.field("STUDENT_SCHOOL_CLASS_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}