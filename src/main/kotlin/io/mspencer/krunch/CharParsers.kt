package io.mspencer.krunch

abstract class CharParsers : Parsers() {
    val skipWhitespace = true

    val indent = BlockParser<CharReader, Int> { input ->
        val input = if (skipWhitespace) {
            input.skipWhitespace(onlyCurrentLine = true)
        } else {
            input
        }

        input.take("[\t ]+".toRegex()).map { it.value.length }.also { println("Took indent: $it") }
    }
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
    fun until(vararg chars: Char) = chars
            .map { it.toString() }.joinToString("")
            .let { Regex.escape(it) }
            .also { println("Until regex: [^$it]+") }
            .let { literal("[^$it]+".toRegex()) }
            .also { println("Regex matched: $it") }

    fun <A>Parser<CharReader, A>.between(left: Char, right: Char) = literal(left) then this before literal(right)
    fun <A>Parser<CharReader, A>.between(left: String, right: String) = literal(left) then this before literal(right)

    fun between(left: Char, right: Char) = until(right).between(left, right)

    fun parens() = between('(', ')')
    fun brackets() = between('[', ']')
    fun braces() = between('{', '}')

    fun <A>parens(parser: Parser<CharReader, A>) = parser.between('(', ')')
    fun <A>brackets(parser: Parser<CharReader, A>) = parser.between('[', ']')
    fun <A>braces(parser: Parser<CharReader, A>) = parser.between('{', '}')

    infix fun <A>Char.then(parser: Parser<CharReader, A>) = literal(this) then parser
    infix fun <A>String.then(parser: Parser<CharReader, A>) = literal(this) then parser

    fun Parser<CharReader, String>.trim() = this map { it.trim() }

    fun <T>parse(target: Parser<CharReader, T>, source: CharSequence): T {
        val input = CharReader(source)
        val result = (target before endOfFile).apply(input)

        when (result) {
            is Result.Ok -> return result.matched
            is Result.Failure -> throw Exception("Failure: ${result.message} (${result.input.position})\n\n${result.input.position.longString}")
            is Result.Error -> throw Exception("ERROR: ${result.message} (${result.input.position})\n\n${result.input.position.longString}")
        }
    }

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