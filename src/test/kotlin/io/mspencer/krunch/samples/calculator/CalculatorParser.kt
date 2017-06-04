package io.mspencer.krunch.samples.calculator

import io.mspencer.krunch.*
import io.mspencer.krunch.samples.calculator.CalculatorToken.*

object CalculatorParser : TokenParsers<CalculatorToken, Double>() {
    val expressionRef: Parser<TokenReader<CalculatorToken>, Double> = ref { expression }

    val number = NUMBER::class map { it.value }

    val parens = expressionRef.between(LPAREN::class, RPAREN::class)
    val factor = oneOf(number, parens)

    val term = factor and some(oneOf(OP('*'), OP('/')) and factor) map {
        it.second.fold(it.first) { left, (op, right) ->
            when (op.value) {
                '*' -> left * right
                '/' -> left / right
                else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
            }
        }
    }

    val expression = term and some(oneOf(OP('+'), OP('-')) and term) map {
        it.second.fold(it.first) { left, (op, right) ->
            when (op.value) {
                '+' -> left + right
                '-' -> left - right
                else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
            }
        }
    }

    override val lexer = ::CalculatorLexer
    override val goal = expression
}