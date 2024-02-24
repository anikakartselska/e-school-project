@file:UseSerializers(
    BigDecimalSerializer::class
)

package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.schoolClass.SchoolClass
import com.nevexis.backend.serializers.BigDecimalSerializer
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

sealed class DetailsForUser {

    data class DetailsForStudent(
        val schoolClass: SchoolClass,
        val numberInClass: BigDecimal
    ) : DetailsForUser()

    data class DetailsForParent(
        val child: User
    ) : DetailsForUser()
}
