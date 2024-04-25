package com.nevexis.backend.schoolManagement.school_lessons

import com.nevexis.backend.schoolManagement.school_class.SchoolClassWithPlan
import com.nevexis.backend.schoolManagement.school_schedule.WorkingHour
import com.nevexis.backend.schoolManagement.users.TeacherView

data class PlannedSchoolLesson(
    val room: String,
    val workingHour: WorkingHour,
    val teacher: TeacherView,
    val subject: String,
    val schoolClass: SchoolClassWithPlan
)