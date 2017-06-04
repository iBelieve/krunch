package io.mspencer.krunch.samples.calculator

import io.mspencer.krunch.*

object OneStepCalculatorParser : CharParsers() {
    val expressionRef: Parser<CharReader, Double> = ref { expression }

    val number = literal("[\\d-.]+".toRegex()) map { it.toDouble() }

    val parens = expressionRef.between('(', ')')
    val factor = oneOf(number, parens)

    val term = factor and some(oneOf('*', '/') and factor) map {
        it.second.fold(it.first) { left, (op, right) ->
            when (op) {
                '*' -> left * right
                '/' -> left / right
                else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
            }
        }
    }

    val expression = term and some(oneOf('+', '-') and term) map {
        it.second.fold(it.first) { left, (op, right) ->
            when (op) {
                '+' -> left + right
                '-' -> left - right
                else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
            }
        }
    }

    val goal = expression

    fun parse(source: String): Double {
        val input = CharReader(source)
        val result = (goal before endOfFile).apply(input)

        when (result) {
            is Result.Ok -> return result.matched
            is Result.Failure -> throw Exception("Failure: " + result.message)
            is Result.Error -> throw Exception("ERROR: " + result.message)
        }
    }
}