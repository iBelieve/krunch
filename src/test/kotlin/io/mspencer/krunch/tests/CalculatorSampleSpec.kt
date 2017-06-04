package io.mspencer.krunch.tests

import io.mspencer.krunch.samples.calculator.OneStepCalculatorParser
import io.mspencer.krunch.samples.calculator.eval
import org.amshove.kluent.shouldEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.system.measureTimeMillis

class CalculatorSampleSpek : Spek({
    describe("the calculator") {
        it("should parse a single number") {
            eval("10.2") shouldEqualTo 10.2
        }

        it("should parse basic addition") {
            eval("5 + 2") shouldEqualTo 7.0
        }

        it("should parse multiple terms") {
            eval("10 - 4 + 3") shouldEqualTo 9.0
        }

        it("should parse multiplication first") {
            eval("10 - 4 * 2") shouldEqualTo 2.0
        }

        it("should parse expressions in parens") {
            eval("30 * (4 - 2)") shouldEqualTo 60.0
        }

        it("should be quick") {
            val expression = "100 * (2 + (3 - 1)) * (1 * 2 - 3 / 4) * (1 - 2) / (4 + 8)"
            val count = 1000

            val time = measureTimeMillis {
                repeat(count) {
                    eval(expression)
                }
            }
            val time2 = measureTimeMillis {
                repeat(count) {
                    OneStepCalculatorParser.parse(expression)
                }
            }

            println("Time: $time, $time2")
        }
    }
})