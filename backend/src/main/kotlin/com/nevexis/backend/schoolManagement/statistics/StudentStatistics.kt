package com.nevexis.backend.schoolManagement.statistics

data class StudentStatistics(
    val success: Double,
    val grades: Int,
    val absences: Int,
    val feedback: Int,
    val examinations: Int,
    val events: Int,
    val placeInClass: Int,
    val placeInGraduationClass: Int,
    val placeInSchool: Int,
)