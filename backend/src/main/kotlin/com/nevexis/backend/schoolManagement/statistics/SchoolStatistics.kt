package com.nevexis.backend.schoolManagement.statistics

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.users.StudentView
import java.math.BigDecimal

data class SchoolStatistics(
    val success: BigDecimal,
    val grades: Int,
    val absences: Int,
    val feedback: Int,
    val examinations: Int,
    val events: Int,
    val schoolClassToAverageGrade: List<Pair<SchoolClass, BigDecimal?>>,
    val studentToAverageGrade: List<Pair<StudentView, BigDecimal?>>
)


