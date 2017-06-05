package io.mspencer.ledger

import java.util.*

data class PostingAST(val status: Status, val account: String, val amount: Amount?, val balance: Amount?,
                      val comment: String)

data class TransactionAST(val date: Date, val date2: Date?, val status: Status, val code: String?,
                          val description: String, val comment: String, val postings: List<PostingAST>)

data class PeriodicTransactionAST(val title: String, val postings: List<PostingAST>)

data class JournalAST(val entries: List<Any>)