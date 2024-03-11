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
    val endYear: LocalDate,
    val firstSemester: Int,
    val secondSemester: Int
)

data class SchoolPeriodWithSchoolIds(
    val id: BigDecimal,
    val startYear: LocalDate,
    val endYear: LocalDate,
    val firstSemester: BigDecimal,
    val secondSemester: BigDecimal,
    val schoolIds: List<BigDecimal>
)