package com.nevexis.backend.schoolManagement.schoolClass

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.`demo-project`.jooq.tables.records.SchoolClassRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SchoolClassService : BaseService() {

    @Autowired
    @Lazy
    private lateinit var userService: UserService


    fun getSchoolClassById(schoolClassId: BigDecimal, dsl: DSLContext = db) =
        recordSelectOnConditionStep(dsl).where(SCHOOL_CLASS.ID.eq(schoolClassId)).fetchAny()?.map {
            mapRecordToInternalModel(it)
        } ?: error("School class with id:${schoolClassId} does not exist")

    fun getSchoolClassByNameSchoolAndPeriod(name: String, schoolId: BigDecimal, periodId: BigDecimal, dsl: DSLContext) =
        recordSelectOnConditionStep(dsl).where(
            SCHOOL_CLASS.NAME.eq(name),
            SCHOOL_CLASS.SCHOOL_ID.eq(schoolId),
            SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(periodId)
        ).fetchAny()?.map {
            mapRecordToInternalModel(it)
        } ?: throw SMSError("NOT_FOUND", "School class $name does not exist")

    fun getSchoolClasses(dsl: DSLContext = db): List<SchoolClass> =
        recordSelectOnConditionStep(dsl).orderBy(SCHOOL_CLASS.NAME)
            .fetch().map {
                mapRecordToInternalModel(it)
            }


    fun getAllSchoolClassesFromSchoolAndPeriod(schoolId: BigDecimal, periodId: BigDecimal) =
        recordSelectOnConditionStep().where(
            SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(periodId).and(SCHOOL_CLASS.SCHOOL_ID.eq(schoolId))
        )
            .orderBy(SCHOOL_CLASS.NAME).map {
                mapRecordToInternalModel(it)
            }


    private fun recordSelectOnConditionStep(dsl: DSLContext = db) =
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
        id = id!!.toInt(),
        name = name!!,
        mainTeacher = mainTeacher,
        schoolId = schoolId!!.toInt(),
        schoolPeriodId = schoolPeriodId!!.toInt()
    )
}