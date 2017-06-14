package io.mspencer.krunch.samples.calculator

import io.mspencer.krunch.*

object OneStepCalculatorParser : RegexParsers<Double>() {
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

    override val goal = expression
}