package com.nevexis.backend.schoolManagement.school

import java.math.BigDecimal

data class School(
    val id: BigDecimal,
    val schoolName: String,
    val city: String,
    val address: String
)