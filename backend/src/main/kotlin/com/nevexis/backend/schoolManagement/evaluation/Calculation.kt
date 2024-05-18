package com.nevexis.backend.schoolManagement.evaluation

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

interface Calculation {
    val scale: Int
        get() = 14

    fun BigDecimal.round(newScale: Int = 2): BigDecimal = this.setScale(newScale, RoundingMode.HALF_UP)

    operator fun BigDecimal.plus(a: BigDecimal): BigDecimal = this.setScale(14, RoundingMode.HALF_UP)
        .add(a.setScale(scale, RoundingMode.HALF_UP), MathContext.DECIMAL64)

    operator fun BigDecimal.minus(a: BigDecimal): BigDecimal = this.setScale(scale, RoundingMode.HALF_UP)
        .subtract(a.setScale(scale, RoundingMode.HALF_UP), MathContext.DECIMAL64)


    operator fun BigDecimal.times(a: BigDecimal): BigDecimal = this.setScale(scale, RoundingMode.HALF_UP)
        .multiply(a.setScale(scale, RoundingMode.HALF_UP), MathContext.DECIMAL64)


    operator fun BigDecimal.div(a: BigDecimal): BigDecimal = this.setScale(scale, RoundingMode.HALF_UP)
        .divide(a.setScale(scale, RoundingMode.HALF_UP), MathContext.DECIMAL64)

}