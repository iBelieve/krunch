package io.mspencer.ledger

import io.mspencer.ledger.models.*
import java.util.*

object JournalValidator {
    fun assignBalance(posting: PostingAST, currentAccountBalances: Map<String, MixedAmount>) = posting.let {
        val amount = if (it.balance != null) {
            val currentBalance = currentAccountBalances[it.account] ?: MixedAmount()

            if (it.amount != null) {
                val newBalance = (currentBalance + it.amount)[it.balance.commodity]

                require(newBalance == it.balance) { "Balance assertion failed: $newBalance != ${it.balance}:\n\n${posting.region.longString}" }

                it.amount
            } else {
                it.balance - currentBalance
            }
        } else {
            it.amount
        }

        Pair(it, amount)
    }

    fun fillInMissingAmount(transation: TransactionAST, postings: List<Pair<PostingAST, Amount?>>): List<Pair<PostingAST, MixedAmount>> {
        val currentBalance = postings
                .map { it.second }
                .filterNotNull()
                .fold(MixedAmount()) { acc, amount -> acc + amount }

        val missingAmountsCount = postings.count { it.second == null }

        if (missingAmountsCount > 1) {
            throw IllegalStateException("Unable to balance transaction: can't have more than one posting with no amount:\n\n${transation.region.longString}")
        } else if (missingAmountsCount == 0 && !currentBalance.isZero) {
            throw IllegalStateException("Transaction does not balance: off by $currentBalance:\n\n${transation.region.longString}")
        }

        return postings.map {
            val amount = if (it.second == null) {
                -currentBalance
            } else {
                MixedAmount(it.second!!)
            }

            Pair(it.first, amount)
        }
    }

    fun process(ast: TransactionAST, currentAccountBalances: Map<String, MixedAmount>): Transaction {
        val postings = ast.postings
                .map { assignBalance(it, currentAccountBalances) }
                .let { fillInMissingAmount(ast, it) }
                .map { (posting, amount) ->
                    posting.run {
                        Posting(status.takeUnless { it == Status.UNCLEARED } ?: ast.status,
                                account, amount)
                    }
                }

        return ast.run { Transaction(date, date2, status, code, description, postings) }
    }

    fun process(ast: JournalAST): Journal {
        val (accountBalances, transactions) = ast.entries.filterIsInstance<TransactionAST>()
                .fold(Pair(mapOf<String, MixedAmount>(), listOf<Transaction>())) { (currentAccountBalances, transactions), ast ->
                    val transaction = process(ast, currentAccountBalances)
                    Pair(currentAccountBalances + transaction, transactions + transaction)
                }

        return Journal(accountBalances, transactions)
    }
}

private operator fun Map<String, MixedAmount>.plus(transaction: Transaction): Map<String, MixedAmount> {
    val mutableSelf = toMutableMap()

    transaction.postings.forEach {
        mutableSelf[it.account] = mutableSelf[it.account]?.plus(it.amount) ?: it.amount
    }

    return Collections.unmodifiableMap(mutableSelf)
}
