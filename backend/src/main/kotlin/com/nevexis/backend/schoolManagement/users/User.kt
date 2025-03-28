package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val personalNumber: String,
    val gender: Gender,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val password: String? = null,
    val status: RequestStatus,
    val roles: List<SchoolUserRole>? = null
)

@Serializable
data class OneRoleUser(
    val id: Int?,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val personalNumber: String,
    val gender: Gender? = null,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val role: SchoolUserRole
)

@Serializable
data class UserView(
    val id: Int,
    val email: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val roles: List<SchoolRole>,
    val status: RequestStatus? = null,
    val profilePicture: String? = null
)

@Serializable
data class StudentView(
    val id: Int,
    val email: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val numberInClass: Int?,
    val schoolClassId: Int,
    val schoolClassName: String
)

@Serializable
data class TeacherView(
    val id: Int,
    val email: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val username: String,
    val qualifiedSubjects: Set<String>,
)


enum class SchoolRole {
    ADMIN, TEACHER, STUDENT, PARENT;

}

fun getTranslationFromBulgarianToEnglish(bulgarianGender: String): Gender {
    return when (bulgarianGender.uppercase()) {
        "МЪЖ" -> Gender.MALE
        "ЖЕНА" -> Gender.FEMALE
        else -> throw SMSError("Данните не са намерени", "Полът не съществува")
    }
}


enum class Gender {
    MALE, FEMALE
}