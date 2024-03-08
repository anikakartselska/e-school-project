package com.nevexis.backend.schoolManagement.school_period

import java.math.BigDecimal
import java.time.LocalDate

data class SchoolPeriod(
    val id: BigDecimal,
    val startYear: LocalDate,
    val endYear: LocalDate,
    val firstSemester: BigDecimal,
    val secondSemester: BigDecimal
)

data class SchoolPeriodWithSchoolIds(
    val id: BigDecimal,
    val startYear: LocalDate,
    val endYear: LocalDate,
    val firstSemester: BigDecimal,
    val secondSemester: BigDecimal,
    val schoolIds: List<BigDecimal>
)