package com.nevexis.backend.schoolManagement.school_class

import com.nevexis.backend.schoolManagement.school_calendar.Shift
import com.nevexis.backend.schoolManagement.users.UserView
import kotlinx.serialization.Serializable

@Serializable
data class SchoolClass(
    val id: Int? = null,
    val name: String,
    val mainTeacher: UserView? = null,
    val schoolId: Int,
    val schoolPeriodId: Int,
    val shifts: ShiftsForSemesters
)

@Serializable
data class SchoolClassWithPlan(
    val id: Int? = null,
    val name: String,
    val mainTeacher: UserView? = null,
    val plan: Map<String, Int>,
    val shifts: ShiftsForSemesters
)

@Serializable
data class ShiftsForSemesters(
    val firstSemester: Shift,
    val secondSemester: Shift,
)