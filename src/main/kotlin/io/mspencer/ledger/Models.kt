package io.mspencer.ledger

import java.util.*

enum class Status {
    UNCLEARED, CLEARED, PENDING
}

data class Transaction(val date: Date, val date2: Date?, val status: Status, val code: String?,
                       val description: String, val comment: String)

data class Journal(val entries: List<Any>)