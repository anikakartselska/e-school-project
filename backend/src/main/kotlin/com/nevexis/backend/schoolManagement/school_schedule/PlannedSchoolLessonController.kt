package com.nevexis.backend.schoolManagement.school_schedule

import com.nevexis.backend.schoolManagement.school_calendar.CalendarService
import com.nevexis.backend.schoolManagement.school_lessons.SchoolLessonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class PlannedSchoolLessonController {

    @Autowired
    private lateinit var plannedSchoolLessonsService: PlannedSchoolLessonsService

    @Autowired
    private lateinit var schoolLessonService: SchoolLessonService

    @Autowired
    private lateinit var calendarService: CalendarService


    @GetMapping("/get-planned-school-lessons-for-school")
    suspend fun getPlannedSchoolLessonsForSchool(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<PlannedSchoolLesson> {
        val calendar = calendarService.getSchoolCalendarForSchoolAndPeriod(schoolId, periodId)
        val plannedSchoolLessons =
            plannedSchoolLessonsService.getAndSavePlannedSchoolLessonsForEachClass(schoolId, periodId)
//        schoolLessonService.createSchoolLessons(plannedSchoolLessons, calendar!!, Semester.SECOND, periodId, schoolId)
        return plannedSchoolLessons
    }


}