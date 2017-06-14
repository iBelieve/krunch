package io.mspencer.ledger.models

import java.util.*

data class Transaction(val date: Date, val date2: Date?, val status: Status, val code: String?,
                       val description: String, val comment: String, val postings: List<Posting>) {
    val payee = description.takeIf { "|" in it }?.substring(0, description.indexOf('|'))?.trim() ?: description
    val note = description.takeIf { "|" in it }?.substring(description.indexOf('|') + 1)?.trim()

    val assetChange get() = postings
            .filter { it.account.startsWith("assets:", ignoreCase = true) }
            .map { it.amount }
            .fold(MixedAmount()) { sum, amount ->
                sum + amount
            }
}