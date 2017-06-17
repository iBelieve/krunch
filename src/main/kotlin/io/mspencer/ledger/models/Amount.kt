package io.mspencer.ledger.models

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
                  val multiplier: Boolean = false, val side: CommoditySide = CommoditySide.LEFT) {
    val isZero = quantity.compareTo(BigDecimal.ZERO) == 0

    operator fun plus(amount: Amount) = copy(quantity = this.quantity + amount.quantity)

    operator fun plus(quantity: BigDecimal) = copy(quantity = this.quantity + quantity)

    operator fun minus(amount: MixedAmount): Amount {
        return this.copy(quantity = quantity - (amount[commodity]?.quantity ?: BigDecimal.ZERO))
    }

    operator fun unaryMinus() = this.copy(quantity = -quantity)

    override fun equals(other: Any?): Boolean {
        if (other !is Amount)
            return false

        return commodity == other.commodity && quantity.compareTo(other.quantity) == 0 && price == other.price &&
                multiplier == other.multiplier
    }

    override fun toString(): String {
        val quantityString = String.format(Locale.getDefault(), "%,.2f", quantity.setScale(2))

        val amount = if (side == CommoditySide.LEFT) {
            "$commodity $quantityString"
        } else {
            "$quantityString $commodity"
        }

        return amount + when (price) {
            is Price.UnitPrice -> " @ ${price.amount}"
            is Price.TotalPrice -> " @@ ${price.amount}"
            null -> ""
        }
    }
}

data class MixedAmount(val amounts: Map<String, Amount> = mapOf()) {
    val isZero = amounts.filterNot { it.value.isZero }.isEmpty()
    val isPositive = amounts.filterNot { it.value.quantity > BigDecimal.ZERO }.isEmpty()

    constructor(amount: Amount) : this(listOf(amount))
    constructor(amounts: List<Amount>) : this(amounts.groupBy { it.commodity }.mapValues { it.value.reduce { acc, amount -> acc + amount.quantity } })

    fun map(block: (Amount) -> Amount) = this.copy(amounts = amounts.mapValues { block(it.value) })

    operator fun contains(commodity: String) = commodity in amounts

    operator fun get(commodity: String) = amounts[commodity]

    operator fun plus(amount: MixedAmount): MixedAmount {
        val replacements = amount.amounts.values.map {
            it.commodity to (amounts[it.commodity]?.plus(it) ?: it)
        }

        return MixedAmount(amounts + replacements)
    }

    operator fun plus(amount: Amount): MixedAmount {
        val replacement = amount.commodity to (amounts[amount.commodity]?.plus(amount) ?: amount)

        return MixedAmount(amounts + replacement)
    }

    operator fun unaryMinus() = map { -it }

    override fun toString() = amounts.values.joinToString("\n")
}