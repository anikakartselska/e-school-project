@file:UseSerializers(
    LocalDateSerializer::class
)


package com.nevexis.backend.schoolManagement.school_period

import com.nevexis.backend.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class SchoolPeriod(
    val id: Int? = null,
    val startYear: Int,
    val endYear: Int
)

enum class Semester {
    FIRST, SECOND, YEARLY
}

data class SchoolPeriodWithSchoolIds(
    val id: BigDecimal,
    val startYear: Int,
    val endYear: Int,
    val schoolIds: List<BigDecimal>
)