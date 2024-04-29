package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.users.UserView
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Subject(
    val id: Int,
    val name: String,
    val teacher: UserView?
)

data class SubjectWithSchoolClassInformation(
    val id: BigDecimal,
    val name: String,
    val schoolClass: SchoolClass? = null,
)