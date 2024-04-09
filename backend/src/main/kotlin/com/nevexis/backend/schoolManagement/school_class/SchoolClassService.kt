package com.nevexis.backend.schoolManagement.school_class

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.data_import.ImportService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolClassRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolClassService : BaseService() {

    @Autowired
    @Lazy
    private lateinit var userService: UserService

    @Autowired
    @Lazy
    private lateinit var schoolRolesService: SchoolRolesService

    @Autowired
    @Lazy
    private lateinit var importService: ImportService

    fun saveUpdateSchoolClass(
        schoolClass: SchoolClass,
        studentsFromClassFile: ByteArray?,
        userId: BigDecimal
    ): BigDecimal {
        val schoolClassId = schoolClass.id?.toBigDecimal() ?: getSchoolClassSeqNextVal()
        db.transaction { transaction ->
            (transaction.dsl().selectFrom(SCHOOL_CLASS).where(SCHOOL_CLASS.ID.eq(schoolClassId)).fetchAny()
                ?: transaction.dsl().newRecord(
                    SCHOOL_CLASS
                ))
                .apply {
                    id = schoolClassId
                    name = schoolClass.name
                    schoolId = schoolClass.schoolId.toBigDecimal()
                    schoolPeriodId = schoolClass.schoolPeriodId.toBigDecimal()
                    mainTeacherRoleId =
                        schoolRolesService.getTeacherRoleId(
                            schoolClass.mainTeacher?.id!!.toBigDecimal(),
                            schoolClass.schoolPeriodId.toBigDecimal(),
                            schoolClass.schoolId.toBigDecimal()
                        )
                }.store()
            if (studentsFromClassFile != null) {
                importService.createRequestsFromUsersExcel(
                    studentsFromClassFile,
                    schoolClass.schoolPeriodId.toBigDecimal(),
                    schoolClass.schoolId.toBigDecimal(),
                    SchoolRole.STUDENT,
                    schoolClassId,
                    userId,
                    transaction.dsl()
                )
            }
        }
        return schoolClassId
    }

    fun synchronizeNumbersInClass(schoolClassId: BigDecimal, periodId: BigDecimal) {
        db.transaction { transaction ->
            transaction.dsl().batchUpdate(userService.studentRecordSelectOnConditionStep(transaction.dsl())
                .where(
                    STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId).and(
                        SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
                            .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                            .and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
                            .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                    )
                )
                .orderBy(USER.FIRST_NAME, USER.MIDDLE_NAME, USER.LAST_NAME)
                .fetchInto(STUDENT_SCHOOL_CLASS)
                .mapIndexed { index, studentSchoolClassRecord ->
                    studentSchoolClassRecord.apply {
                        numberInClass = (index + 1).toBigDecimal()
                    }
                }).execute()
        }

    }

    fun getSchoolClassById(schoolClassId: BigDecimal, dsl: DSLContext = db) =
        studentSchoolClassRecordSelectOnConditionStep(dsl).where(SCHOOL_CLASS.ID.eq(schoolClassId)).fetchAny()?.map {
            mapRecordToInternalModel(it)
        } ?: error("School class with id:${schoolClassId} does not exist")

    fun getSchoolClassByNameSchoolAndPeriod(name: String, schoolId: BigDecimal, periodId: BigDecimal, dsl: DSLContext) =
        studentSchoolClassRecordSelectOnConditionStep(dsl).where(
            SCHOOL_CLASS.NAME.eq(name),
            SCHOOL_CLASS.SCHOOL_ID.eq(schoolId),
            SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(periodId)
        ).fetchAny()?.map {
            mapRecordToInternalModel(it)
        } ?: throw SMSError("NOT_FOUND", "School class $name does not exist")

    fun getSchoolClasses(dsl: DSLContext = db): List<SchoolClass> =
        studentSchoolClassRecordSelectOnConditionStep(dsl).orderBy(SCHOOL_CLASS.NAME)
            .fetch().map {
                mapRecordToInternalModel(it)
            }


    fun getAllSchoolClassesFromSchoolAndPeriod(schoolId: BigDecimal, periodId: BigDecimal) =
        studentSchoolClassRecordSelectOnConditionStep().where(
            SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(periodId).and(SCHOOL_CLASS.SCHOOL_ID.eq(schoolId))
        )
            .orderBy(SCHOOL_CLASS.NAME).map {
                mapRecordToInternalModel(it)
            }


    private fun studentSchoolClassRecordSelectOnConditionStep(dsl: DSLContext = db) =
        dsl.select(
            SCHOOL_CLASS.asterisk(), USER.asterisk(), SCHOOL_USER_ROLE.asterisk(), SCHOOL_USER.asterisk(),
            SCHOOL_USER_PERIOD.asterisk()
        )
            .from(SCHOOL_CLASS)
            .leftJoin(SCHOOL_USER_ROLE)
            .on(SCHOOL_CLASS.MAIN_TEACHER_ROLE_ID.eq(SCHOOL_USER_ROLE.ID))
            .leftJoin(USER).on(
                SCHOOL_USER_ROLE.USER_ID.eq(
                    USER.ID
                )
            ).leftJoin(SCHOOL_USER)
            .on(SCHOOL_USER.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER_PERIOD)
            .on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))


    fun mapRecordToInternalModel(it: Record): SchoolClass {
        val role = SchoolRole.valueOf(it.get(SCHOOL_USER_ROLE.ROLE)!!)
        return it.into(SchoolClassRecord::class.java).mapToInternalModel(
            mainTeacher = userService.mapToUserView(it, listOf(role))
        )
    }

    private fun SchoolClassRecord.mapToInternalModel(mainTeacher: UserView) = SchoolClass(
        id = id?.toInt(),
        name = name!!,
        mainTeacher = mainTeacher,
        schoolId = schoolId!!.toInt(),
        schoolPeriodId = schoolPeriodId!!.toInt()
    )

    fun getSchoolClassSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_CLASS_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}