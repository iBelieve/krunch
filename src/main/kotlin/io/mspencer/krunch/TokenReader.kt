package io.mspencer.krunch

data class TokenReader<T : Token>(val lexer: Lexer<T>, override val index: Int = 0) : Reader<TokenReader<T>, T> {
    override val atEnd get() = lexer.atEnd(index)
    override val first get() = lexer.tokenAt(index)

    override fun remainder(nextIndex: Int) = this.copy(index = nextIndex)
}