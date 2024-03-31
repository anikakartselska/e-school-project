@file:UseSerializers(
    BigDecimalSerializer::class
)

package com.nevexis.backend.schoolManagement.requests


import com.nevexis.backend.schoolManagement.users.OneRoleUser
import com.nevexis.backend.schoolManagement.users.User
import com.nevexis.backend.schoolManagement.users.UserView
import com.nevexis.backend.serializers.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.math.BigDecimal
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

data class AdditionalRequestInformation(
    val valueId: BigDecimal,
    val status: RequestStatus? = null,
    val schoolId: BigDecimal,
    val periodId: BigDecimal
)

sealed class RequestValue {
    data class UserRegistration(
        val user: User,
        val status: RequestStatus? = null
    ) : RequestValue()

    data class Role(
        val oneRoleUser: OneRoleUser,
        val status: RequestStatus? = null
    ) : RequestValue()
}

@Serializable(with = RequestValueSerializer::class)
sealed class RequestValueJson {
    @Serializable
    data class UserRegistration(
        val schoolUserPeriodId: Int,
        val status: RequestStatus? = null
    ) : RequestValueJson()

    @Serializable
    data class Role(
        val schoolUserRolePeriodId: Int,
        val status: RequestStatus? = null
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