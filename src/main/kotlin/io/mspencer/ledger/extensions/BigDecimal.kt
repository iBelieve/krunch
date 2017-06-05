package io.mspencer.ledger.extensions

import java.math.BigDecimal
import java.text.DecimalFormat

fun parseBigDecimal(value: String): BigDecimal {
    val format = DecimalFormat()
    format.isParseBigDecimal = true

    return format.parse(value) as BigDecimal
}