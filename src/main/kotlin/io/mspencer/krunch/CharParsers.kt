package io.mspencer.krunch

abstract class CharParsers : Parsers() {
    val skipWhitespace = true

    val endOfFile = CharParser<Unit> { input ->
        if (input.atEnd) {
            Result.Ok(Unit, input.index, input)
        } else {
            Result.Failure("Expected end of file!", input.index, input)
        }
    }

    fun literal(char: Char) = CharParser { it.take(char) }
    fun literal(string: String) = CharParser { it.take(string) }
    fun literal(regex: Regex) = CharParser { it.take(regex).map { it.groupValues[0] } }
    fun match(regex: Regex) = CharParser { it.take(regex) }
    fun oneOf(vararg chars: Char) = CharParser { it.take(chars) }

    fun <A>Parser<CharReader, A>.between(left: Char, right: Char) = literal(left) then this before literal(right)
    fun <A>Parser<CharReader, A>.between(left: String, right: String) = literal(left) then this before literal(right)

    fun <A>parens(parser: Parser<CharReader, A>) = parser.between('(', ')')
    fun <A>brackets(parser: Parser<CharReader, A>) = parser.between('[', ']')
    fun <A>braces(parser: Parser<CharReader, A>) = parser.between('{', '}')

    inner class CharParser<out A>(val block: (CharReader) -> Result<CharReader, A>) : Parser<CharReader, A> {
        override fun apply(input: CharReader): Result<CharReader, A> {
            if (skipWhitespace) {
                return block(input.skipWhitespace())
            } else {
                return block(input)
            }
        }
    }
}