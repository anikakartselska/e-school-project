package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.users.UserView
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Subject(
    val id: Int,
    val name: String,
    val teacher: UserView?,
    val forClass: Int
)

data class SubjectWithSchoolClassInformation(
    val id: BigDecimal,
    val name: String,
    val schoolClass: String?,
    val schoolId: BigDecimal,
    val schoolPeriodId: BigDecimal
)