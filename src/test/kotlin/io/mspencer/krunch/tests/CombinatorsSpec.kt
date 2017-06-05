package io.mspencer.krunch.tests

import io.mspencer.krunch.CharParsers
import io.mspencer.krunch.CharReader
import io.mspencer.krunch.Result
import io.mspencer.krunch.and
import org.amshove.kluent.`should be instance of`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object SampleParser : CharParsers() {
    val andParser = literal('(') and literal(')')
}

class CombinatorsSpec : Spek({
    describe("and") {
        it("should error if the first unique token is not found") {
            SampleParser.andParser.apply(CharReader("(a")) `should be instance of` Result.Error::class
        }
    }
})