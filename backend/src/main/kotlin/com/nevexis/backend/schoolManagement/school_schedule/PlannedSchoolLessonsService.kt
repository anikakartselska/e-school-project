package com.nevexis.backend.schoolManagement.school_schedule

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school.SchoolService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassService
import com.nevexis.backend.schoolManagement.school_lessons.PlannedSchoolLesson
import com.nevexis.backend.schoolManagement.subject.SubjectService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.references.PLANNED_SCHOOL_LESSON
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
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
        periodId: BigDecimal
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
        }.insert()

        return getPlannedSchoolLessonsForSchoolAndPeriod(schoolId, periodId)
    }

    fun getPlannedSchoolLessonsForSchoolAndPeriod(schoolId: BigDecimal, periodId: BigDecimal) =
        db.select(PLANNED_SCHOOL_LESSON.PLANNED_SCHOOL_LESSONS).from(PLANNED_SCHOOL_LESSON)
            .where(
                PLANNED_SCHOOL_LESSON.SCHOOL_ID.eq(schoolId).and(PLANNED_SCHOOL_LESSON.SCHOOL_PERIOD_ID.eq(periodId))
            ).fetchAnyInto(String::class.java).let {
                decodeFromString(Schedule.serializer(), it!!)
            }.planedSchoolLessons


    fun getPlannedSchoolLessonSeqNextVal(): BigDecimal =
        db.select(DSL.field("PLANNED_SCHOOL_LESSON_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}