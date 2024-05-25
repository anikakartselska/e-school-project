package com.nevexis.backend.schoolManagement.statistics

import java.math.BigDecimal

data class StudentStatistics(
    val success: BigDecimal? = null,
    val grades: Int,
    val absences: Int,
    val feedback: Int,
    val examinations: Int,
    val events: Int,
    val placeInClass: Int,
    val placeInGraduationClass: Int,
    val placeInSchool: Int,
)

data class EvaluationsCount(
    val gradesCount: Int,
    val absencesCount: Int,
    val feedbackCount: Int,
)