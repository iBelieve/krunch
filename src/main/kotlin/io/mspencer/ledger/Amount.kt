package io.mspencer.ledger

import java.math.BigDecimal
import java.util.*

enum class Status {
    UNCLEARED, CLEARED, PENDING
}

enum class CommoditySide {
    LEFT, RIGHT
}

sealed class Price {
    data class TotalPrice(val amount: Amount) : Price()
    data class UnitPrice(val amount: Amount) : Price()
}

data class Amount(val commodity: String, val quantity: BigDecimal = BigDecimal.ZERO, val price: Price? = null,
                  val multiplier: Boolean = false, val side: CommoditySide = CommoditySide.LEFT)