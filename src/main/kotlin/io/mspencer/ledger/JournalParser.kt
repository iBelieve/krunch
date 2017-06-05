package io.mspencer.ledger

import io.mspencer.krunch.*
import io.mspencer.ledger.extensions.parseBigDecimal
import khronos.toDate
import java.util.*

object JournalParser : RegexParsers<JournalAST>() {
    // Don't allow whitespace to flow onto multiple lines
    override val isWhitespace = { it: Char -> it.category == CharCategory.SPACE_SEPARATOR }

    //<editor-fold desc="Common parsers">
    val comment = match(";\\s*(.*)\\s*$".toRegex(RegexOption.MULTILINE)) map { it.groupValues[1] }
    val commentLine = comment before endOfLine

    val followingComment = optional(comment) before endOfLine prepended some(indent then commentLine) map { it.joinToString("\n") }

    val date = literal("[\\d\\-./]+".toRegex()) map {
        try {
            it.toDate("yyyy/MM/dd")
        } catch (ex: Exception) {
            try {
                it.toDate("MM/dd")
            } catch (ex2: Exception) {
                throw ex
            }
        }
    }

    val datePair = date and optional('=' then date) also {
        val date = it.first

        (it.second as? Date)?.let { if (it.year == 70) it.year = date.year }
    }

    val status = optional(oneOf('!', '*')) map {
        when (it) {
            '!' -> Status.PENDING
            '*' -> Status.CLEARED
            else -> Status.UNCLEARED
        }
    }
    //</editor-fold>

    //<editor-fold desc="Amount parsing">
    val amountRef = ref { amount }

    val sign = optional(oneOf('-', '+')) map { it ?: '+' }
    val multiplier = optional('*') map { it != null }

    val quotedCommoditySymbol = '\"' then literal(".*?(?=[;\n\"]|$)".toRegex()) before '\"'
    val simpleCommoditySymbol = literal("[^0123456789\\-+.@*;\n \"{}=]+".toRegex())
    val commoditySymbol = quotedCommoditySymbol or simpleCommoditySymbol

    val number = literal("[+\\-]?[\\d,.]+".toRegex()) map { parseBigDecimal(it) }

    val totalPrice = '@' then amountRef flatMap Price::TotalPrice
    val unitPrice = amountRef flatMap Price::UnitPrice

    val priceAmount = optional(literal('@') then totalPrice or unitPrice)

    val leftSymbolAmount = sign and multiplier and commoditySymbol and number and priceAmount flatMap { sign, multiplier, commodity, number, price ->
        val quantity = when (sign) {
            '-' -> number.negate()
            else -> number
        }
        Amount(commodity = commodity, quantity = quantity, price = price, multiplier = multiplier,
                side = CommoditySide.LEFT)
    }

    val rightSymbolAmount = multiplier and number and commoditySymbol and priceAmount flatMap { multiplier, number, commodity, price ->
        Amount(commodity = commodity, quantity = number, price = price, multiplier = multiplier,
                side = CommoditySide.RIGHT)
    }

    val noSymbolAmount = multiplier and number and priceAmount flatMap { multiplier, number, price ->
        // TODO: Better default commodity
        Amount(commodity = "$", quantity = number, price = price, multiplier = multiplier)
    }

    val amount: Parser<CharReader, Amount> = leftSymbolAmount or rightSymbolAmount or noSymbolAmount
    //</editor-fold>

    //<editor-fold desc="Transaction parsing">
    val code = between('(', ')')

    val description = until(';', '\n')

    val accountName = literal(".+?(?=( {2})|\n|$)".toRegex())

    val balance = '=' then amount

    val posting = indent then status and accountName and optional(amount) and optional(balance) and followingComment flatMap ::PostingAST

    val postings = some(posting)

    val transaction = datePair and status and optional(code) and description and followingComment and postings flatMap ::TransactionAST

    val periodicTransaction = '~' then restOfLine and postings flatMap ::PeriodicTransactionAST
    //</editor-fold>

    val unexpectedIndent = indent then error("Unexpected indent")

    val entry = skip('\n' or unexpectedIndent or commentLine) or periodicTransaction or transaction

    override val goal = many(entry) map ::JournalAST
}