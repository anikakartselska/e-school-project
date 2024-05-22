package com.nevexis.backend.schoolManagement.school_lessons

import com.nevexis.backend.schoolManagement.school_period.Semester
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
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

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/fetch-available-rooms-for-school-lesson")
    suspend fun fetchAvailableRoomsForSchoolLesson(
        @RequestBody schoolLesson: SchoolLesson,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = schoolLessonService.getAvailableRoomsForSchoolLesson(schoolLesson, schoolId, periodId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @DeleteMapping("/delete-school-lessons")
    suspend fun deleteSchoolLessons(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam semester: Semester
    ) = schoolLessonService.deleteSchoolLessonsAndTheTablesRelatedToIt(schoolId, periodId, semester)

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/existing-school-lessons-for-semester")
    suspend fun checkExistingSchoolLessonsForSemester(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam semester: Semester
    ) = schoolLessonService.checkIfThereAreSchoolLessonsForSemester(schoolId, periodId, semester)

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/generate-school-lessons")
    suspend fun generateSchoolLessons(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam semester: Semester
    ) = schoolLessonService.generateSchoolLessonsForTheWholeSemester(semester, periodId, schoolId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/fetch-available-teachers-for-school-lesson")
    suspend fun fetchAvailableTeachersForSchoolLesson(
        @RequestBody schoolLesson: SchoolLesson,
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ) = schoolLessonService.getAvailableTeachersForSchoolLesson(schoolLesson, schoolId, periodId)

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
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

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/update-school-lesson")
    suspend fun updateSchoolLesson(
        @RequestBody schoolLesson: SchoolLesson
    ) = schoolLessonService.updateSchoolLesson(schoolLesson)
}