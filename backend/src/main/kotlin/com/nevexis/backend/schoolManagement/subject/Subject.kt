package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.users.UserView
import java.math.BigDecimal

data class Subject(
    val id: BigDecimal,
    val name: String,
    val teacher: UserView?,
    val forClass: BigDecimal,
    val semester: Semester
)

data class SubjectWithSchoolClassInformation(
    val id: BigDecimal,
    val name: String,
    val schoolClass: String?,
    val schoolId: BigDecimal,
    val schoolPeriodId: BigDecimal
)