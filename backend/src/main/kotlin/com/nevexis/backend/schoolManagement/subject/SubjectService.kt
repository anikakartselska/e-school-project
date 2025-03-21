package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.school_schedule.PlannedSchoolLesson
import com.nevexis.backend.schoolManagement.users.SchoolRole
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.SubjectRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
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
            .where(USER.ID.eq(teacherId))
            .and(SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId))
            .and(SCHOOL_USER.SCHOOL_ID.eq(schoolId))
            .fetch().map { record ->
                record.into(SubjectRecord::class.java)
                    .into(SubjectWithSchoolClassInformation::class.java).copy(
                        schoolClass = schoolClassService.mapRecordToInternalModel(record),
                        semester = Semester.valueOf(record.get(SCHOOL_CLASS_SUBJECT.SEMESTER, String::class.java)!!)
                    )
            }.distinctBy { it.id }

    fun getAllSubjects(): List<String> = db.select(SUBJECT_NAME.NAME).from(SUBJECT_NAME).fetchInto(String::class.java)

    fun generateSubjectRecordsFromPlannedSchoolLessons(
        plannedSchoolLessons: List<PlannedSchoolLesson>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        semester: Semester,
        dsl: DSLContext,
    ) = plannedSchoolLessons.distinctBy { Pair(it.subject, it.teacher) }.map { plannedSchoolLesson ->
        val subjectRecord = dsl.selectFrom(SUBJECT).where(
            SUBJECT.NAME.eq(plannedSchoolLesson.subject).and(
                SUBJECT.TEACHER_ID.eq(plannedSchoolLesson.teacher.id.toBigDecimal())
                    .and(SUBJECT.SCHOOL_ID.eq(schoolId))
                    .and(SUBJECT.SCHOOL_PERIOD_ID.eq(periodId))
            )
        ).fetchAny() ?: dsl.newRecord(SUBJECT).apply {
            id = getSubjectSeqNextVal()
            name = plannedSchoolLesson.subject
            teacherId = plannedSchoolLesson.teacher.id.toBigDecimal()
            this.schoolId = schoolId
            schoolPeriodId = periodId
        }

        subjectRecord to (dsl.selectFrom(SCHOOL_CLASS_SUBJECT).where(
            SCHOOL_CLASS_SUBJECT.SEMESTER.eq(semester.name)
                .and(SCHOOL_CLASS_SUBJECT.SCHOOL_CLASS_ID.eq(plannedSchoolLesson.schoolClass.id?.toBigDecimal()))
                .and(SCHOOL_CLASS_SUBJECT.SUBJECT_ID.eq(subjectRecord.id))
        ).fetchAny() ?: dsl.newRecord(SCHOOL_CLASS_SUBJECT).apply {
            this.semester = semester.name
            this.schoolClassId = plannedSchoolLesson.schoolClass.id?.toBigDecimal()
            this.subjectId = subjectRecord.id
        })
    }.let { pairs ->
        dsl.batchStore(pairs.map { it.first }).execute()
        dsl.batchStore(pairs.map { it.second }).execute()
        pairs.map { it.first }
    }


    fun deleteSubjectsForSchoolAndPeriod(
        semesterToPlannedSchoolLessons: Map<Semester, List<PlannedSchoolLesson>>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        semester: Semester,
        dsl: DSLContext = db,
    ) {
        val semesterToPairSubjectIdTeacherId = semesterToPlannedSchoolLessons.mapValues { (_, plannedSchoolLessons) ->
            plannedSchoolLessons.map { Pair(it.subject, it.teacher.id) }.distinct()
        }

        dsl.transaction { transaction ->
            val subjectsToDelete = semesterToPairSubjectIdTeacherId[semester]?.filter {
                if (semester == Semester.FIRST) {
                    semesterToPairSubjectIdTeacherId[Semester.SECOND]
                } else {
                    semesterToPairSubjectIdTeacherId[Semester.FIRST]
                }?.contains(it) != true
            } ?: emptyList()

            val subjectRecordsToDelete = transaction.dsl().selectFrom(SUBJECT)
                .where(SUBJECT.SCHOOL_ID.eq(schoolId))
                .and(SUBJECT.SCHOOL_PERIOD_ID.eq(periodId))
                .fetch()
                .filter {
                    subjectsToDelete.contains(Pair(it.name!!, it.teacherId!!.toInt()))
                }

            transaction.dsl().deleteFrom(SCHOOL_CLASS_SUBJECT)
                .where(SCHOOL_CLASS_SUBJECT.SEMESTER.eq(semester.name))
                .execute()

            transaction.dsl().batchDelete(subjectRecordsToDelete).execute()
        }
    }

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
            }.distinctBy { subject -> subject.id }

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
                teacher = userService.mapToUserView(record, listOf(SchoolRole.TEACHER))
            )
        }

    fun getSubjectSeqNextVal(): BigDecimal =
        db.select(DSL.field("SUBJECT_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }


}