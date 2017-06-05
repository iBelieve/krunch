package io.mspencer.krunch.tests

import io.mspencer.ledger.JournalParser
import khronos.toString
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class TransactionSpec : Spek({
    describe("the transaction parser") {
        it("should parse a simple transaction header") {
            val result = JournalParser.parse(JournalParser.transaction, "2017/05/07 * (1120) Grace Lutheran Church")

            result.date.toString("yyyy/MM/dd") shouldEqualTo "2017/05/07"
            result.date2.shouldBeNull()
            result.code.shouldNotBeNull()
            result.code!! shouldEqualTo "1120"
            result.description shouldEqualTo "Grace Lutheran Church"
        }

        it("should parse a description with a (") {
            val result = JournalParser.parse(JournalParser.transaction, "2017/05/07 * ( Somewhere fun")

            result.date.toString("yyyy/MM/dd") shouldEqualTo "2017/05/07"
            result.date2.shouldBeNull()
            result.code.shouldBeNull()
            result.description shouldEqualTo "( Somewhere fun"
        }

        it("should parse a transaction header with a following comment") {
            val result = JournalParser.parse(JournalParser.transaction,
                    """
                    |2017/05/03=05/10 ! Options for Women ; First line
                    |    ; Donated to Gabrielle Aschbrenner's pro-life fundraiser
                    """.trimMargin())

            result.date.toString("yyyy/MM/dd") shouldEqualTo "2017/05/03"
            result.date2.shouldNotBeNull()
            result.date2!!.toString("yyyy/MM/dd") shouldEqualTo "2017/05/10"
            result.code.shouldBeNull()
            result.description shouldEqualTo "Options for Women"
            result.comment shouldEqualTo "First line\nDonated to Gabrielle Aschbrenner's pro-life fundraiser"
        }

        it("should parse a full transaction") {
            val result = JournalParser.parse(JournalParser.transaction,
                    """
                    |2017/05/03 * Options for Women
                    |    ; Donated to Gabrielle Aschbrenner's pro-life fundraiser
                    |    Expenses:Giving:Charity                            $ 30.00
                    |    Assets:Checking
                    |
                    """.trimMargin())

            result.date.toString("yyyy/MM/dd") shouldEqualTo "2017/05/03"
            result.date2.shouldBeNull()
            result.code.shouldBeNull()
            result.description shouldEqualTo "Options for Women"
            result.comment shouldEqualTo "Donated to Gabrielle Aschbrenner's pro-life fundraiser"
            result.postings.size shouldEqualTo 2
        }
    }
})