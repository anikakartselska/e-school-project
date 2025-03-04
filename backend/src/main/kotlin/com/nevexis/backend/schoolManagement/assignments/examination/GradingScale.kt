@file:UseSerializers(
    BigDecimalSerializer::class,
    LocalDateTimeSerializer::class
)

package com.nevexis.backend.schoolManagement.assignments.examination

import com.nevexis.backend.serializers.BigDecimalSerializer
import com.nevexis.backend.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class GradingScale(
    val interval2: Interval,
    val interval3: Interval,
    val interval4: Interval,
    val interval5: Interval,
    val interval6: Interval,
)

@Serializable
data class Interval(
    val startingPoints: BigDecimal,
    val endingPoints: BigDecimal
)