package io.mspencer.ledger.models

import java.util.*

data class Transaction(val date: Date, val date2: Date?, val status: Status, val code: String?,
                       val description: String, val postings: List<Posting>)