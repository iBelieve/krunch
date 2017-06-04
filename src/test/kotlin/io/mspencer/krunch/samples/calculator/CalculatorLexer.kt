package io.mspencer.krunch.samples.calculator

import io.mspencer.krunch.Lexer
import io.mspencer.krunch.map
import io.mspencer.krunch.oneOf
import io.mspencer.krunch.samples.calculator.CalculatorToken.*

class CalculatorLexer(source: CharSequence) : Lexer<CalculatorToken>(source) {
    val lparen = literal('(') map { LPAREN() }
    val rparen = literal(')') map { RPAREN() }
    val op = oneOf('+', '-', '*', '/') map { OP(it) }
    val number = literal("[\\d-.]+".toRegex()) map { NUMBER(it.toDouble()) }

    override val tokens = oneOf(lparen, rparen, op, number)
}