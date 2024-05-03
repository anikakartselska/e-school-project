package com.nevexis.backend.schoolManagement.school_schedule

data class SubjectAndClassesCount(
    val subjectName: String,
    val classesPerWeek: Int,
    val classesPerSchoolYear: Int
)