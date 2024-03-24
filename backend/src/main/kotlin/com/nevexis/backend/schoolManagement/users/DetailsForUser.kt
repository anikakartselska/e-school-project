
package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.schoolClass.SchoolClass
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
        val numberInClass: Int? = null
    ) : DetailsForUser()

    @Serializable
    @SerialName("DetailsForParent")
    data class DetailsForParent(
        val child: OneRoleUser
    ) : DetailsForUser()
}

object DetailsForUserSerializer :
    JsonContentPolymorphicSerializer<DetailsForUser>(DetailsForUser::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "child" in element.jsonObject -> DetailsForUser.DetailsForParent.serializer()
        "schoolClass" in element.jsonObject -> DetailsForUser.DetailsForStudent.serializer()
        else -> error("There are no other details for user")
    }
}
