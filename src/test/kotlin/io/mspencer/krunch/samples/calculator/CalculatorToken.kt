package io.mspencer.krunch.samples.calculator

import io.mspencer.krunch.Token

sealed class CalculatorToken : Token {
    class LPAREN : CalculatorToken()
    class RPAREN : CalculatorToken()
    data class OP(val value: Char) : CalculatorToken()
    data class NUMBER(val value: Double) : CalculatorToken()
}