package com.nevexis.backend.schoolManagement.school_schedule

import com.nevexis.backend.schoolManagement.school.RoomToSubjects
import com.nevexis.backend.schoolManagement.school_class.SchoolClassWithPlan
import com.nevexis.backend.schoolManagement.users.TeacherView
import kotlinx.serialization.Serializable

@Serializable
data class PlannedSchoolLesson(
    val room: RoomToSubjects,
    val workingHour: WorkingHour,
    val teacher: TeacherView,
    val subject: String,
    val schoolClass: SchoolClassWithPlan
)