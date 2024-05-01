package com.nevexis.backend.schoolManagement.school_lessons

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SchoolLessonController {
    @Autowired
    private lateinit var schoolLessonService: SchoolLessonService

    @GetMapping("/fetch-school-lessons-for-school-class-week-school-and-period")
    suspend fun fetchSchoolLessonsForSchoolClassWeekSchoolAndPeriod(
        @RequestParam schoolClassId: BigDecimal,
        @RequestParam weekNumber: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<SchoolLesson> = schoolLessonService.getSchoolLessonsForSchoolClassWeekSchoolAndPeriod(
        schoolClassId,
        weekNumber,
        schoolId,
        periodId
    )

    @GetMapping("/fetch-school-lessons-for-teacher-week-school-and-period")
    suspend fun fetchSchoolLessonsForTeacherWeekSchoolAndPeriod(
        @RequestParam teacherId: BigDecimal,
        @RequestParam weekNumber: BigDecimal,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<SchoolLesson> = schoolLessonService.getSchoolLessonsForTeacherWeekSchoolAndPeriod(
        teacherId,
        weekNumber,
        schoolId,
        periodId
    )

    @GetMapping("/fetch-school-lesson-by-id")
    suspend fun fetchSchoolLessonById(
        @RequestParam schoolLessonId: BigDecimal
    ): SchoolLesson = schoolLessonService.getSchoolLessonById(
        schoolLessonId
    )

    @PostMapping("/update-school-lesson")
    fun updateSchoolLesson(
        @RequestBody schoolLesson: SchoolLesson
    ) = schoolLessonService.updateSchoolLesson(schoolLesson)
}