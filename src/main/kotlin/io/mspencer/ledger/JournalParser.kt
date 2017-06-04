package io.mspencer.ledger

import io.mspencer.krunch.*
import khronos.toDate
import java.util.*

object JournalParser : RegexParsers<Journal>() {
    val date = literal("[\\d\\-./]+".toRegex()) map {
        println("Date: $it")
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

        println("Date Pair: $it")

        (it.second as? Date)?.let { if (it.year == 70) it.year = date.year }
    }

    val status = optional(oneOf('!', '*')) map {
        println("Status: $it")
        when (it) {
            '!' -> Status.PENDING
            '*' -> Status.CLEARED
            else -> Status.UNCLEARED
        }
    }

    val code = between('(', ')').trim()

    val description = until(';', '\n').trim()

    val comment = match(";\\s*(.*)\\s*$".toRegex(RegexOption.MULTILINE)).map { it.groupValues[1] }

    val followingComment = optional(comment) prepended some(indent then comment) map { it.joinToString("\n") }

    val transaction = datePair and status and optional(code) and description and followingComment flatMap ::Transaction

    val entry = transaction

    override val goal = many(entry) map ::Journal
}