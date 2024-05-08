package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.users.UserView
import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    val id: Int,
    val name: String,
    val teacher: UserView?
)

@Serializable
data class SubjectWithSchoolClassInformation(
    val id: Int,
    val name: String,
    val schoolClass: SchoolClass? = null,
)