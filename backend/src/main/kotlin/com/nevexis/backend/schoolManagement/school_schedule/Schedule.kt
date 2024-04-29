package com.nevexis.backend.schoolManagement.school_schedule

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val planedSchoolLessons: List<PlannedSchoolLesson>,
    val fitness: Double
)