package com.nevexis.backend.schoolManagement.schoolClass

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.`demo-project`.jooq.tables.records.SchoolClassRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_CLASS
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class SchoolClassService : BaseService() {

    @Autowired
    @Lazy
    private lateinit var userService: UserService


//    fun getSchoolClassById(schoolClassId: BigDecimal, schoolId: BigDecimal, dsl: DSLContext) =
//        dsl.select(SCHOOL_CLASS.asterisk(), USER.asterisk()).from(SCHOOL_CLASS).leftJoin(USER).on(
//            SCHOOL_CLASS.MAIN_TEACHER.eq(
//                USER.ID
//            )
//        ).where(SCHOOL_CLASS.ID.eq(schoolClassId)).fetchAny()?.map {
//            mapRecordToInternalModel(it)
//        }

    fun getSchoolClasses(dsl: DSLContext = db): List<SchoolClass> =
        dsl.select(SCHOOL_CLASS.asterisk(), USER.asterisk()).from(SCHOOL_CLASS).leftJoin(USER).on(
            SCHOOL_CLASS.MAIN_TEACHER.eq(
                USER.ID
            )
        ).fetch().map {
            mapRecordToInternalModel(it)
        }


    fun mapRecordToInternalModel(it: Record) =
        it.into(SchoolClassRecord::class.java).mapToInternalModel(
            mainTeacher = userService.mapToUserView(it.into(UserRecord::class.java), listOf(SchoolRole.TEACHER))
        )

    private fun SchoolClassRecord.mapToInternalModel(mainTeacher: UserView) = SchoolClass(
        id = id!!,
        name = name!!,
        mainTeacher = mainTeacher,
        schoolId = schoolId!!,
        schoolPeriodId = schoolPeriodId!!
    )
}