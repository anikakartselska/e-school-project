package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.SubjectRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SubjectService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    fun getAllSubjectsTaughtByTeacher(
        teacherId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): List<SubjectWithSchoolClassInformation> =
        db.select(
            SUBJECT.asterisk(),
            SCHOOL_CLASS_SUBJECT.asterisk(),
            SCHOOL_CLASS.asterisk(),
            USER.asterisk(),
            SCHOOL_USER_ROLE.asterisk(),
            SCHOOL_USER.asterisk(),
            SCHOOL_USER_PERIOD.asterisk()
        )
            .from(SUBJECT)
            .leftJoin(SCHOOL_CLASS_SUBJECT).on(SUBJECT.ID.eq(SCHOOL_CLASS_SUBJECT.SUBJECT_ID))
            .leftJoin(SCHOOL_CLASS).on(SCHOOL_CLASS_SUBJECT.SCHOOL_CLASS_ID.eq(SCHOOL_CLASS.ID))
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
            .fetch().map { record ->
                record.into(SubjectRecord::class.java)
                    .into(SubjectWithSchoolClassInformation::class.java).copy(
                        schoolClass = schoolClassService.mapRecordToInternalModel(record)
                    )
            }

    fun getAllSubjects(): List<String> = db.select(SUBJECT_NAME.NAME).from(SUBJECT_NAME).fetchInto(String::class.java)


    fun getAllSubjectsBySchoolClassId(
        schoolClassId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): List<Subject> =
        db.select(
            SUBJECT.asterisk(), USER.asterisk(), SCHOOL_CLASS_SUBJECT.asterisk(),
            SCHOOL_USER.asterisk(),
            SCHOOL_USER_PERIOD.asterisk()
        ).from(SUBJECT)
            .leftJoin(SCHOOL_CLASS_SUBJECT).on(SUBJECT.ID.eq(SCHOOL_CLASS_SUBJECT.SUBJECT_ID))
            .leftJoin(USER).on(SUBJECT.TEACHER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER).on(SCHOOL_USER.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))
            .where(SCHOOL_CLASS_SUBJECT.SCHOOL_CLASS_ID.eq(schoolClassId))
            .and(SUBJECT.SCHOOL_ID.eq(schoolId))
            .and(SUBJECT.SCHOOL_PERIOD_ID.eq(periodId))
            .fetch()
            .map {
                mapToInternalModel(it)
            }

    fun getSubjectsById(
        subjectId: BigDecimal,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ): Subject? =
        db.select(
            SUBJECT.asterisk(), USER.asterisk(),
            SCHOOL_USER.asterisk(),
            SCHOOL_USER_PERIOD.asterisk()
        ).from(SUBJECT)
            .leftJoin(USER).on(SUBJECT.TEACHER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER).on(SCHOOL_USER.USER_ID.eq(USER.ID))
            .leftJoin(SCHOOL_USER_PERIOD).on(SCHOOL_USER_PERIOD.SCHOOL_USER_ID.eq(SCHOOL_USER.ID))
            .where(SUBJECT.SCHOOL_ID.eq(schoolId))
            .and(SUBJECT.SCHOOL_PERIOD_ID.eq(periodId))
            .and(SUBJECT.ID.eq(subjectId))
            .fetchAny()
            ?.let {
                mapToInternalModel(it)
            }


    fun mapToInternalModel(record: Record) = record.into(SubjectRecord::class.java)
        .map {
            Subject(
                id = (it as SubjectRecord).id!!.toInt(),
                name = it.name!!,
                teacher = userService.mapToUserView(record, listOf(SchoolRole.TEACHER)),
                forClass = it.forClass!!.toInt(),
            )
        }

}