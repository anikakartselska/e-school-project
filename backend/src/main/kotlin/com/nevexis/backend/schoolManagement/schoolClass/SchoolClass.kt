package com.nevexis.backend.schoolManagement.schoolClass

import com.nevexis.backend.schoolManagement.users.UserView
import kotlinx.serialization.Serializable

@Serializable
data class SchoolClass(
    val id: Int? = null,
    val name: String,
    val mainTeacher: UserView? = null,
    val schoolId: Int,
    val schoolPeriodId: Int
)