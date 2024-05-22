package com.nevexis.backend.schoolManagement.school_schedule

import com.nevexis.backend.schoolManagement.school_period.Semester
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class PlannedSchoolLessonController {

    @Autowired
    private lateinit var plannedSchoolLessonsService: PlannedSchoolLessonsService

    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/get-planned-school-lessons-for-school")
    suspend fun getPlannedSchoolLessonsForSchool(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam semester: Semester
    ): List<PlannedSchoolLesson> =
        plannedSchoolLessonsService.getPlannedSchoolLessonsForSchoolAndPeriod(schoolId, periodId, semester)
            ?: emptyList()

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/generate-planned-school-lessons-for-school")
    suspend fun generatePlannedSchoolLessonsForSchool(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam semester: Semester
    ): List<PlannedSchoolLesson> =
        plannedSchoolLessonsService.getAndSavePlannedSchoolLessonsForEachClass(schoolId, periodId, semester)

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/existing-planned-school-lessons-for-semester")
    suspend fun checkExistingPlannedSchoolLessonsForSemester(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal,
        @RequestParam semester: Semester
    ) = plannedSchoolLessonsService.checkIfThereArePlannedSchoolLessonsForSemester(schoolId, periodId, semester)

}