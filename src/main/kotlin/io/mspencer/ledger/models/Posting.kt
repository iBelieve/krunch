package io.mspencer.ledger.models

data class Posting(val status: Status, val account: String, val amount: MixedAmount)