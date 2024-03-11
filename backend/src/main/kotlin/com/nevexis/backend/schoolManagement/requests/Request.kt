@file:UseSerializers(
    BigDecimalSerializer::class
)

package com.nevexis.backend.schoolManagement.requests


import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.backend.schoolManagement.users.roles.SchoolUserRole
import com.nevexis.backend.schoolManagement.users.school_user.SchoolUser
import com.nevexis.backend.serializers.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.time.LocalDateTime

data class Request(
    val id: Int,
    val requestedByUser: UserView,
    val requestValue: RequestValue,
    val requestDate: LocalDateTime,
    val requestStatus: RequestStatus = RequestStatus.PENDING,
    val resolvedByUser: UserView? = null,
    val resolvedDate: LocalDateTime? = null
)

sealed class RequestValue {
    data class UserRegistration(
        val schoolUser: SchoolUser,
    ) : RequestValue()

    data class Role(
        val schoolUserRole: SchoolUserRole
    ) : RequestValue()
}

@Serializable(with = RequestValueSerializer::class)
sealed class RequestValueJson {
    @Serializable
    data class UserRegistration(
        val schoolUserPeriodId: Int,
    ) : RequestValueJson()

    @Serializable
    data class Role(
        val schoolUserRolePeriodId: Int
    ) : RequestValueJson()
}

object RequestValueSerializer :
    JsonContentPolymorphicSerializer<RequestValueJson>(RequestValueJson::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "schoolUserPeriodId" in element.jsonObject -> RequestValueJson.UserRegistration.serializer()
        "schoolUserRolePeriodId" in element.jsonObject -> RequestValueJson.Role.serializer()
        else -> error("There are no other types of requests")
    }
}

enum class RequestStatus {
    PENDING,
    REJECTED,
    APPROVED
}