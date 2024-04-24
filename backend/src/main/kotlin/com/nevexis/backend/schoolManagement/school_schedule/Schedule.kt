package com.nevexis.backend.schoolManagement.school_schedule

data class Schedule(
    val planedSchoolLessons: List<PlannedSchoolLesson>,
    val fitness: Double
)