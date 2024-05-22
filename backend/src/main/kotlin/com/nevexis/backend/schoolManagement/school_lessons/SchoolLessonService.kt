package com.nevexis.backend.schoolManagement.school_lessons

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school.RoomToSubjects
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.school_calendar.Calendar
import com.nevexis.backend.schoolManagement.school_calendar.CalendarService
import com.nevexis.backend.schoolManagement.school_calendar.Shift
import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.school_schedule.PlannedSchoolLesson
import com.nevexis.backend.schoolManagement.school_schedule.PlannedSchoolLessonsService
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.subject.SubjectService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.`demo-project`.jooq.tables.records.SchoolLessonRecord
import com.nevexis.`demo-project`.jooq.tables.records.SubjectRecord
import com.nevexis.`demo-project`.jooq.tables.references.SCHOOL_LESSON
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.Configuration
import org.jooq.DSLContext
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

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var schoolService: SchoolService

    @Autowired
    private lateinit var plannedSchoolLessonsService: PlannedSchoolLessonsService

    @Autowired
    private lateinit var calendarService: CalendarService

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
                val teacher = if (subject.teacher?.id!!.toBigDecimal() == schoolLessonRecord.teacherId) {
                    subject.teacher
                } else {
                    userService.getUserViewsById(
                        schoolLessonRecord.teacherId!!,
                        schoolLessonRecord.schoolPeriodId!!,
                        schoolLessonRecord.schoolId!!
                    )
                }
                mapRecordToInternalModel(schoolLessonRecord, subject, schoolClass, teacher)
            } ?: throw SMSError("Данните не са намерени", "Урокът,който търсите не съществува или е бил изтрит")

    fun getAvailableRoomsForSchoolLesson(
        schoolLesson: SchoolLesson,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<RoomToSubjects> {
        val busyRooms: List<RoomToSubjects> = db.select(SCHOOL_LESSON.ROOM).from(SCHOOL_LESSON)
            .where(SCHOOL_LESSON.SEMESTER.eq(schoolLesson.semester.name))
            .and(SCHOOL_LESSON.WEEK.eq(schoolLesson.week.toBigDecimal())).and(SCHOOL_LESSON.SCHOOL_ID.eq(schoolId))
            .and(SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId))
            .and(SCHOOL_LESSON.WORKING_HOUR.eq(Json.encodeToString(schoolLesson.workingDay)))
            .fetchInto(String::class.java).map { Json.decodeFromString(it) }
        val allRooms = schoolService.getAllRoomsFromSchool(schoolId)
        return allRooms.filter { room -> busyRooms.find { busyRoom -> busyRoom.room == room.room } != null } + schoolLesson.room
    }

    fun getAvailableTeachersForSchoolLesson(
        schoolLesson: SchoolLesson,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<UserView> {
        val busyTeachers = db.select(SCHOOL_LESSON.TEACHER_ID).from(SCHOOL_LESSON)
            .where(SCHOOL_LESSON.SEMESTER.eq(schoolLesson.semester.name))
            .and(SCHOOL_LESSON.WEEK.eq(schoolLesson.week.toBigDecimal())).and(SCHOOL_LESSON.SCHOOL_ID.eq(schoolId))
            .and(SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId))
            .and(SCHOOL_LESSON.TEACHER_ID.notEqual(schoolLesson.teacher.id.toBigDecimal()))
            .and(SCHOOL_LESSON.WORKING_HOUR.eq(Json.encodeToString(schoolLesson.workingDay)))
            .fetchInto(BigDecimal::class.java)
        val allTeachersQualified = userService.getAllApprovedTeachersFromSchool(schoolId, periodId).filter {
            it.qualifiedSubjects.contains(schoolLesson.subject.name)
        }
        return allTeachersQualified.filter { !busyTeachers.contains(it.id.toBigDecimal()) }
            .map {
                userService.mapTeacherViewToUserView(it)
            }
    }

    fun updateSchoolLesson(schoolLesson: SchoolLesson) {
        db.selectFrom(SCHOOL_LESSON).where(SCHOOL_LESSON.ID.eq(schoolLesson.id.toBigDecimal())).fetchAny()
            ?.apply {
                id = schoolLesson.id.toBigDecimal()
                startTimeOfLesson = schoolLesson.startTimeOfLesson
                endTimeOfLesson = schoolLesson.endTimeOfLesson
                subjectId = schoolLesson.subject.id.toBigDecimal()
                lessonTopic = schoolLesson.lessonTopic
                room = schoolLesson.room.toString()
                taken = if (schoolLesson.taken) {
                    "Y"
                } else {
                    "N"
                }
                teacherId = schoolLesson.teacher.id.toBigDecimal()
                status = schoolLesson.status.name
            }?.update()
    }

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
                val teacher = if (subject.teacher?.id!!.toBigDecimal() == it.teacherId) {
                    subject.teacher
                } else {
                    userService.getUserViewsById(it.teacherId!!, periodId, schoolId)
                }
                mapRecordToInternalModel(it, subject, schoolClass, teacher)
            }.sortedWith(compareBy(
                { it.workingDay.workingDays.order },
                { it.workingDay.hour }
            ))

    }

    fun getSchoolLessonsForTeacherWeekSchoolAndPeriod(
        teacherId: BigDecimal,
        weekNumber: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<SchoolLesson> {
        val teacher = userService.getUserViewsById(teacherId, periodId, schoolId)
        return db.selectFrom(SCHOOL_LESSON)
            .where(
                SCHOOL_LESSON.TEACHER_ID.eq(teacherId)
                    .and(SCHOOL_LESSON.WEEK.eq(weekNumber))
                    .and(SCHOOL_LESSON.SCHOOL_ID.eq(schoolId))
                    .and(SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId))
            ).fetch()
            .map {
                val schoolClass = schoolClassService.getSchoolClassById(it.schoolClassId!!)
                val subject = subjectService.getSubjectsById(it.subjectId!!, periodId, schoolId)!!
                mapRecordToInternalModel(it, subject, schoolClass, teacher)
            }.sortedWith(compareBy(
                { it.workingDay.workingDays.order },
                { it.workingDay.hour }
            ))

    }

    fun generateSchoolLessonsForTheWholeSemester(
        semester: Semester,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) {
        val plannedSchoolLessons =
            plannedSchoolLessonsService.getPlannedSchoolLessonsForSchoolAndPeriod(schoolId, periodId, semester)
                ?: throw SMSError(
                    "Данните не са намерени",
                    "Няма генерирана програма за ${
                        if (semester == Semester.FIRST) {
                            "първи срок"
                        } else {
                            "втори срок"
                        }
                    }"
                )
        val calendar = calendarService.getSchoolCalendarForSchoolAndPeriod(schoolId, periodId) ?: throw SMSError(
            "Данните не са намерени",
            "Няма училището няма добавен календар за текущата учебна година"
        )

        createSchoolLessons(plannedSchoolLessons, calendar, semester, periodId, schoolId)
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
            }.flatten().apply {
                transaction.dsl().batchInsert(this).execute()
            }
        }
    }

    fun deleteSchoolLessonsAndTheTablesRelatedToIt(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        semester: Semester
    ) {
        db.transaction { transaction ->
            val plannedSchoolLessonsFirstSemester =
                Semester.FIRST to (plannedSchoolLessonsService.getPlannedSchoolLessonsForSchoolAndPeriod(
                    schoolId,
                    periodId,
                    Semester.FIRST
                ) ?: emptyList())
            val plannedSchoolLessonsSecondSemester =
                Semester.SECOND to (plannedSchoolLessonsService.getPlannedSchoolLessonsForSchoolAndPeriod(
                    schoolId,
                    periodId,
                    Semester.SECOND
                ) ?: emptyList())

            plannedSchoolLessonsService.deletePlannedSchoolLessonsForSchoolAndPeriod(
                schoolId,
                periodId,
                semester,
                transaction.dsl()
            )
            deleteSchoolLessons(periodId, schoolId, semester, transaction.dsl())
            subjectService.deleteSubjectsForSchoolAndPeriod(
                mapOf(
                    plannedSchoolLessonsFirstSemester,
                    plannedSchoolLessonsSecondSemester
                ),
                schoolId, periodId, semester, transaction.dsl()
            )
        }
    }

    fun deleteSchoolLessons(
        periodId: BigDecimal,
        schoolId: BigDecimal,
        semester: Semester,
        dslContext: DSLContext = db
    ) = dslContext.delete(SCHOOL_LESSON).where(
        SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId)
            .and(SCHOOL_LESSON.SCHOOL_ID.eq(schoolId))
            .and(SCHOOL_LESSON.SEMESTER.eq(semester.name))
    ).execute()

    fun checkIfThereAreSchoolLessonsForSemester(
        periodId: BigDecimal,
        schoolId: BigDecimal,
        semester: Semester
    ) = db.fetchCount(
        SCHOOL_LESSON, SCHOOL_LESSON.SEMESTER.eq(semester.name), SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId),
        SCHOOL_LESSON.SCHOOL_ID.eq(schoolId)
    ) > 0


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
            room = Json.encodeToString(plannedSchoolLesson.room)
            this.schoolId = schoolId
            schoolPeriodId = periodId
            week = date.get(weekFields.weekOfYear()).toBigDecimal()
            this.semester = semester.name
            this.workingHour = Json.encodeToString(plannedSchoolLesson.workingHour)
            this.status = SchoolLessonStatus.NORMAL.name
            this.teacherId = plannedSchoolLesson.teacher.id.toBigDecimal()
        }
    }

    private fun mapRecordToInternalModel(
        it: SchoolLessonRecord,
        subject: Subject,
        schoolClass: SchoolClass,
        teacher: UserView
    ) = SchoolLesson(
        id = it.id!!.toInt(),
        startTimeOfLesson = it.startTimeOfLesson!!,
        endTimeOfLesson = it.endTimeOfLesson!!,
        subject = subject,
        schoolClass = schoolClass,
        lessonTopic = it.lessonTopic,
        room = Json.decodeFromString(it.room!!),
        taken = it.taken == "Y",
        week = it.week!!.toInt(),
        semester = Semester.valueOf(it.semester!!),
        workingDay = Json.decodeFromString(it.workingHour!!),
        teacher = teacher,
        status = SchoolLessonStatus.valueOf(it.status!!)
    )

    fun getSchoolLessonSeqNextVal(): BigDecimal =
        db.select(DSL.field("SCHOOL_LESSON_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}
