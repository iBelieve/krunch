package io.mspencer.krunch

abstract class Lexer<T : Token>(val source: CharSequence) : CharParsers() {
    abstract val tokens: Parser<CharReader, T>

    val parsedTokens = mutableListOf<Pair<T, CharReader>>()
    var atEnd: Boolean = false

    fun tokenAt(index: Int): T? {
        if (index < parsedTokens.size) {
            return parsedTokens[index].first
        } else if (index == parsedTokens.size) {
            if (atEnd) return null

            return parseNextToken()
        } else {
            throw IllegalStateException("Can only get the next token or a previously parsed token")
        }
    }

    private fun parseNextToken(): T? {
        val input = parsedTokens.lastOrNull()?.second ?: CharReader(source)
        val result = (tokens or endOfFile).apply(input)

        when (result) {
            is Result.Ok -> {
                if (result.matched is Unit) {
                    atEnd = true
                    return null
                }

                @Suppress("UNCHECKED_CAST")
                val matched = result.matched as T

                parsedTokens.add(Pair(matched, result.remainder))
                return matched
            }
            is Result.Error -> throw Exception("ERROR: Unable to parse token: ${result.message}")
            is Result.Failure -> throw Exception("FAILURE: Unable to parse token: ${result.message}")
        }
    }

    fun atEnd(index: Int): Boolean {
        return index >= parsedTokens.size && atEnd
    }
}