package io.mspencer.ledger.models

data class Journal(val accountBalances: Map<String, MixedAmount>, val transactions: List<Transaction>)