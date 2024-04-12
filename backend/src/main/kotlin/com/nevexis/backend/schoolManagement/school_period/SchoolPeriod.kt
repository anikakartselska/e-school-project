@file:UseSerializers(
    LocalDateSerializer::class
)


package com.nevexis.backend.schoolManagement.school_period

import com.nevexis.backend.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class SchoolPeriod(
    val id: Int,
    val startYear: LocalDate,
    val endYear: LocalDate
)

enum class Semester {
    FIRST, SECOND, YEARLY
}

data class SchoolPeriodWithSchoolIds(
    val id: BigDecimal,
    val startYear: LocalDate,
    val endYear: LocalDate,
    val schoolIds: List<BigDecimal>
)