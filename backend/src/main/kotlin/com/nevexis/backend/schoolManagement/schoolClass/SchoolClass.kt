package com.nevexis.backend.schoolManagement.schoolClass

import com.nevexis.backend.schoolManagement.users.UserView
import java.math.BigDecimal

data class SchoolClass(
    val id: BigDecimal,
    val name: String,
    val mainTeacher: UserView?= null,
    val schoolId: BigDecimal,
    val schoolPeriodId: BigDecimal
)