package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.subject.SubjectWithSchoolClassInformation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable(with = DetailsForUserSerializer::class)
sealed class DetailsForUser {
    @Serializable
    @SerialName("DetailsForStudent")
    data class DetailsForStudent(
        val schoolClass: SchoolClass,
        val numberInClass: Int? = null,
        val parents: List<UserView>? = null
    ) : DetailsForUser()

    @Serializable
    @SerialName("DetailsForParent")
    data class DetailsForParent(
        val child: OneRoleUser
    ) : DetailsForUser()


    @Serializable
    @SerialName("DetailsForTeacher")
    data class DetailsForTeacher(
        val qualifiedSubjects: List<String>,
        val teachingSubjects: List<SubjectWithSchoolClassInformation>? = null
    ) : DetailsForUser()

}

object DetailsForUserSerializer :
    JsonContentPolymorphicSerializer<DetailsForUser>(DetailsForUser::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "child" in element.jsonObject -> DetailsForUser.DetailsForParent.serializer()
        "schoolClass" in element.jsonObject -> DetailsForUser.DetailsForStudent.serializer()
        "qualifiedSubjects" in element.jsonObject -> DetailsForUser.DetailsForTeacher.serializer()
        else -> error("There are no other details for user")
    }
}
