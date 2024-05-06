package com.nevexis.backend.schoolManagement.school_plan

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_schedule.SubjectAndClassesCount

data class SchoolPlanForClasses(
    val id: Int? = null,
    val name: String,
    val subjectAndClassesCount: List<SubjectAndClassesCount>,
    val schoolClassesWithTheSchoolPlan: List<SchoolClass>
)