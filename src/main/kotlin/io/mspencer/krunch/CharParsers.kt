package io.mspencer.krunch

abstract class CharParsers : Parsers() {
    open val skipWhitespace = true
    open val isWhitespace: (Char) -> Boolean = Char::isWhitespace

    val indent = BlockParser<CharReader, Int>(false) { input ->
        input.take("[\t ]+".toRegex()).map { it.value.length } //.also { println("Took indent: $it") }
    }

    val endOfFile = CharParser<Unit>(false) { input ->
        if (input.atEnd) {
            Result.Ok(Unit, input.index, input)
        } else {
            Result.Failure("Expected end of file!", input.index, input)
        }
    }

    fun literal(char: Char, unique: Boolean = true) = CharParser(unique) { it.take(char) }
    fun literal(string: String, unique: Boolean = true) = CharParser(unique) { it.take(string) }
    fun literal(regex: Regex, unique: Boolean = true) = CharParser(unique) { it.take(regex).map { it.groupValues[0] } }
    fun match(regex: Regex, unique: Boolean = true) = CharParser(unique) { it.take(regex) }
    fun oneOf(vararg chars: Char, unique: Boolean = true) = CharParser(unique) { it.take(chars) }

    fun optional(char: Char) = optional(literal(char))
    fun optional(string: String) = optional(literal(string))

    fun until(vararg chars: Char) = chars
            .map { it.toString() }.joinToString("")
            .let { Regex.escape(it) }
            //.also { println("Until regex: [^$it]+") }
            .let { literal("[^$it]+".toRegex()) }
            .map { if (skipWhitespace) it.trim() else it }
            //.also { println("Regex matched: $it") }

    fun <A> Parser<CharReader, A>.between(left: Char, right: Char) = literal(left) then this before literal(right)
    fun <A> Parser<CharReader, A>.between(left: String, right: String) = literal(left) then this before literal(right)

    fun between(left: Char, right: Char) = until(right).between(left, right)

    fun parens() = between('(', ')')
    fun brackets() = between('[', ']')
    fun braces() = between('{', '}')

    fun <A> parens(parser: Parser<CharReader, A>) = parser.between('(', ')')
    fun <A> brackets(parser: Parser<CharReader, A>) = parser.between('[', ']')
    fun <A> braces(parser: Parser<CharReader, A>) = parser.between('{', '}')

    infix fun <A> Char.then(parser: Parser<CharReader, A>) = literal(this) then parser
    infix fun <A> String.then(parser: Parser<CharReader, A>) = literal(this) then parser

    infix fun <A> Parser<CharReader, A>.before(char: Char) = this before literal(char)
    infix fun <A> Parser<CharReader, A>.before(string: String) = this before literal(string)

    infix fun <A> Char.or(parser: Parser<CharReader, A>) = literal(this) or parser
    infix fun <A> String.or(parser: Parser<CharReader, A>) = literal(this) or parser

    fun Parser<CharReader, String>.trim() = this map { it.trim() }

    fun <T> parse(target: Parser<CharReader, T>, source: CharSequence): T {
        val input = CharReader(source)
        val result = (target before endOfFile).apply(input)

        when (result) {
            is Result.Ok -> return result.matched
            is Result.Failure -> throw Exception("Failure: ${result.message} (${result.input.position})\n\n${result.input.position.longString}")
            is Result.Error -> throw Exception("ERROR: ${result.message} (${result.input.position})\n\n${result.input.position.longString}")
        }
    }

    inner class CharParser<out A>(override val unique: Boolean, val block: (CharReader) -> Result<CharReader, A>) : Parser<CharReader, A> {
        override fun apply(input: CharReader): Result<CharReader, A> {
            if (skipWhitespace) {
                return block(input.skip(isWhitespace))
            } else {
                return block(input)
            }
        }
    }
}