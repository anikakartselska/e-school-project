package com.nevexis.backend.schoolManagement.school_schedule

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.subject.SubjectService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.references.PLANNED_SCHOOL_LESSON
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PlannedSchoolLessonsService : BaseService() {
    @Autowired
    private lateinit var schoolProgramGenerationService: SchoolProgramGenerationService

    @Autowired
    private lateinit var schoolClassService: SchoolClassService

    @Autowired
    private lateinit var schoolService: SchoolService

    @Autowired
    private lateinit var subjectService: SubjectService

    @Autowired
    private lateinit var userService: UserService
    fun getAndSavePlannedSchoolLessonsForEachClass(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        semester: Semester
    ): List<PlannedSchoolLesson> {
        val schoolClassesWithPlan =
            schoolClassService.getAllSchoolClassesFromSchoolAndPeriodWithPlans(schoolId, periodId)
        val teacherViews = userService.getAllApprovedTeachersFromSchool(schoolId, periodId)
        val subjects = subjectService.getAllSubjects()
        val rooms = schoolService.getAllRoomsFromSchool(schoolId)

        val plannedSchoolLessons = schoolProgramGenerationService.generatePlannedSchoolLessonsForEachClass(
            teacherViews,
            schoolClassesWithPlan,
            subjects,
            rooms
        )
        db.newRecord(PLANNED_SCHOOL_LESSON).apply {
            id = getPlannedSchoolLessonSeqNextVal()
            this.schoolId = schoolId
            this.schoolPeriodId = periodId
            this.plannedSchoolLessons = Json.encodeToString(Schedule(plannedSchoolLessons, 1.0))
            this.semester = semester.name
        }.insert()

        return plannedSchoolLessons
    }


    fun getPlannedSchoolLessonsForSchoolAndPeriod(schoolId: BigDecimal, periodId: BigDecimal, semester: Semester) =
        db.select(PLANNED_SCHOOL_LESSON.PLANNED_SCHOOL_LESSONS).from(PLANNED_SCHOOL_LESSON)
            .where(
                PLANNED_SCHOOL_LESSON.SCHOOL_ID.eq(schoolId)
                    .and(PLANNED_SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId))
                    .and(PLANNED_SCHOOL_LESSON.SEMESTER.eq(semester.name))
            ).fetchAnyInto(String::class.java)?.let {
                decodeFromString(Schedule.serializer(), it)
            }?.planedSchoolLessons

    fun deletePlannedSchoolLessonsForSchoolAndPeriod(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        semester: Semester,
        dslContext: DSLContext = db
    ) = dslContext.delete(PLANNED_SCHOOL_LESSON).where(
        PLANNED_SCHOOL_LESSON.SCHOOL_ID.eq(schoolId)
            .and(PLANNED_SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId))
            .and(PLANNED_SCHOOL_LESSON.SEMESTER.eq(semester.name))
    ).execute()

    fun getPlannedSchoolLessonSeqNextVal(): BigDecimal =
        db.select(DSL.field("PLANNED_SCHOOL_LESSON_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun checkIfThereArePlannedSchoolLessonsForSemester(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        semester: Semester
    ): Boolean = db.fetchCount(
        PLANNED_SCHOOL_LESSON,
        PLANNED_SCHOOL_LESSON.SEMESTER.eq(semester.name),
        PLANNED_SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId),
        PLANNED_SCHOOL_LESSON.SCHOOL_ID.eq(schoolId)
    ) > 0

}