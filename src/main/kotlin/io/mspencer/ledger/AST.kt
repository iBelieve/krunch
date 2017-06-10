package io.mspencer.ledger

import io.mspencer.krunch.Position
import io.mspencer.krunch.Region
import io.mspencer.ledger.models.Amount
import io.mspencer.ledger.models.Status
import java.util.*

data class PostingAST(val status: Status, val account: String, val amount: Amount?, val balance: Amount?,
                      val comment: String, val region: Region)

data class TransactionAST(val date: Date, val date2: Date?, val status: Status, val code: String?, val description: String,
                          val comment: String, val postings: List<PostingAST>, val region: Region)

data class PeriodicTransactionAST(val title: String, val postings: List<PostingAST>, val region: Region)

data class JournalAST(val entries: List<Any>)