package com.nevexis.backend.schoolManagement.school_lessons

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_calendar.Calendar
import com.nevexis.backend.schoolManagement.school_calendar.Shift
import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.school_schedule.PlannedSchoolLesson
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.subject.SubjectService
import com.nevexis.`demo-project`.jooq.tables.records.SchoolLessonRecord
import com.nevexis.`demo-project`.jooq.tables.records.SubjectRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_LESSON
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.WeekFields

@Service
class SchoolLessonService : BaseService() {

    @Autowired
    private lateinit var subjectService: SubjectService

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    fun getSchoolLessonById(schoolLessonId: BigDecimal): SchoolLesson =
        db.selectFrom(SCHOOL_LESSON)
            .where(
                SCHOOL_LESSON.ID.eq(schoolLessonId)
            ).fetchAny()
            ?.let { schoolLessonRecord ->
                val subject = subjectService.getSubjectsById(
                    schoolLessonRecord.subjectId!!,
                    schoolLessonRecord.schoolPeriodId!!,
                    schoolLessonRecord.schoolId!!
                )!!
                val schoolClass = schoolClassService.getSchoolClassById(schoolLessonRecord.schoolClassId!!)
                mapRecordToInternalModel(schoolLessonRecord, subject, schoolClass)
            } ?: throw SMSError("DATA_NOT_FOUND", "School lesson with id:${schoolLessonId} does not exist")


    fun getSchoolLessonsForSchoolClassWeekSchoolAndPeriod(
        schoolClassId: BigDecimal,
        weekNumber: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<SchoolLesson> {
        val schoolClass = schoolClassService.getSchoolClassById(schoolClassId)
        return db.selectFrom(SCHOOL_LESSON)
            .where(
                SCHOOL_LESSON.SCHOOL_CLASS_ID.eq(schoolClassId)
                    .and(SCHOOL_LESSON.WEEK.eq(weekNumber))
                    .and(SCHOOL_LESSON.SCHOOL_ID.eq(schoolId))
                    .and(SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId))
            ).fetch()
            .map {
                val subject = subjectService.getSubjectsById(it.subjectId!!, periodId, schoolId)!!
                mapRecordToInternalModel(it, subject, schoolClass)
            }.sortedWith(compareBy(
                { it.workingDay.workingDays.order },
                { it.workingDay.hour }
            ))

    }

    fun createSchoolLessons(
        plannedSchoolLessons: List<PlannedSchoolLesson>,
        calendar: Calendar,
        semester: Semester,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) {
        val plannedSchoolLessonGroupedByDay = plannedSchoolLessons.groupBy { it.workingHour.workingDays.name }
        db.transaction { transaction ->
            val weekFields = WeekFields.ISO
            val subjectRecords = subjectService.generateSubjectRecordsFromPlannedSchoolLessons(
                plannedSchoolLessons,
                schoolId,
                periodId,
                semester,
                transaction.dsl(),
            ).associateBy { Pair(it.name, it.teacherId) }
            // Get the week number of the current year
            if (semester == Semester.FIRST) {
                generateSequence(calendar.beginningOfYear) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(calendar.endOfFirstSemester) }
                    .toList()
                    .map { date ->
                        plannedSchoolLessonGroupedByDay[date.dayOfWeek.name]?.mapNotNull { plannedSchoolLesson ->
                            val isRestDayOrExamDay =
                                getIsRestDayOrExamDay(calendar, date)
                            if (isRestDayOrExamDay) {
                                null
                            } else {
                                generateSchoolLessonRecordFromPlannedSchoolLesson(
                                    plannedSchoolLesson,
                                    calendar,
                                    date,
                                    transaction,
                                    subjectRecords,
                                    schoolId,
                                    periodId,
                                    weekFields,
                                    semester
                                )
                            }
                        } ?: emptyList()
                    }
            } else {
                generateSequence(calendar.beginningOfSecondSemester) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(calendar.classToEndOfYearDate.values.max()) }
                    .toList()
                    .map { date ->
                        plannedSchoolLessonGroupedByDay[date.dayOfWeek.name]?.mapNotNull { plannedSchoolLesson ->
                            val isRestDayOrExamDay =
                                getIsRestDayOrExamDay(calendar, date)
                            val endOfYearForClass =
                                calendar.classToEndOfYearDate[plannedSchoolLesson.schoolClass.name.dropLast(1)
                                    .toInt()]!!
                            if (isRestDayOrExamDay || date.isAfter(endOfYearForClass)) {
                                null
                            } else {
                                generateSchoolLessonRecordFromPlannedSchoolLesson(
                                    plannedSchoolLesson,
                                    calendar,
                                    date,
                                    transaction,
                                    subjectRecords,
                                    schoolId,
                                    periodId,
                                    weekFields,
                                    semester
                                )
                            }
                        } ?: emptyList()
                    }
            }.apply {
                transaction.dsl().batchInsert(this.flatten()).execute()
            }
        }
    }

    private fun getIsRestDayOrExamDay(
        calendar: Calendar,
        date: LocalDate
    ) = calendar.restDays.map { it.from..it.to }
        .find { range -> range.contains(date) } != null || calendar.examDays.map { it.from..it.to }
        .find { range -> range.contains(date) } != null

    private fun generateSchoolLessonRecordFromPlannedSchoolLesson(
        plannedSchoolLesson: PlannedSchoolLesson,
        calendar: Calendar,
        date: LocalDate,
        transaction: Configuration,
        subjectRecords: Map<Pair<String?, BigDecimal?>, SubjectRecord>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        weekFields: WeekFields,
        semester: Semester
    ): SchoolLessonRecord {
        val schedule = if (plannedSchoolLesson.schoolClass.shifts.firstSemester == Shift.FIRST) {
            calendar.firstShiftSchedule
        } else {
            calendar.secondShiftSchedule
        }
        val startOfSchoolDay = LocalDateTime.of(
            date, LocalTime.parse(schedule.startOfClasses)
        )
        val startTimeOfLesson =
            startOfSchoolDay.plusMinutes((plannedSchoolLesson.workingHour.hour - 1) * (schedule.breakDuration + schedule.durationOfClasses).toLong())
        return transaction.dsl().newRecord(SCHOOL_LESSON).apply {
            id = getSchoolLessonSeqNextVal()
            this.startTimeOfLesson = startTimeOfLesson
            endTimeOfLesson = startTimeOfLesson.plusMinutes(schedule.durationOfClasses.toLong())
            subjectId = subjectRecords[Pair(
                plannedSchoolLesson.subject,
                plannedSchoolLesson.teacher.id.toBigDecimal()
            )]!!.id
            schoolClassId = plannedSchoolLesson.schoolClass.id?.toBigDecimal()
            room = plannedSchoolLesson.room
            this.schoolId = schoolId
            schoolPeriodId = periodId
            week = date.get(weekFields.weekOfYear()).toBigDecimal()
            this.semester = semester.name
            this.workingHour = Json.encodeToString(plannedSchoolLesson.workingHour)
        }
    }

    private fun mapRecordToInternalModel(
        it: SchoolLessonRecord,
        subject: Subject,
        schoolClass: SchoolClass
    ) = SchoolLesson(
        id = it.id!!,
        startTimeOfLesson = it.startTimeOfLesson!!,
        endTimeOfLesson = it.endTimeOfLesson!!,
        subject = subject,
        schoolClass = schoolClass,
        lessonTopic = it.lessonTopic,
        room = it.room!!.toInt(),
        taken = it.taken == "Y",
        week = it.week!!.toInt(),
        semester = Semester.valueOf(it.semester!!),
        workingDay = Json.decodeFromString(it.workingHour!!)
    )

    fun getSchoolLessonSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_LESSON_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}