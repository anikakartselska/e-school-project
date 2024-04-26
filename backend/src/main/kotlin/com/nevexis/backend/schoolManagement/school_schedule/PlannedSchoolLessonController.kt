package com.nevexis.backend.schoolManagement.school_schedule

import com.nevexis.backend.schoolManagement.school_lessons.PlannedSchoolLesson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class PlannedSchoolLessonController {

    @Autowired
    private lateinit var plannedSchoolLessonsService: PlannedSchoolLessonsService

    @GetMapping("/get-planned-school-lessons-for-school")
    suspend fun getPlannedSchoolLessonsForSchool(
        @RequestParam schoolId: BigDecimal,
        @RequestParam periodId: BigDecimal
    ): List<PlannedSchoolLesson> =
        plannedSchoolLessonsService.getPlannedSchoolLessonsForSchoolAndPeriod(schoolId, periodId)

}