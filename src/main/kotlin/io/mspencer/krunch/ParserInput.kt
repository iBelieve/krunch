package io.mspencer.krunch

data class ParserInput(val input: CharSequence, val index: Int = 0, val skipWhitespace: Boolean = false) {
    private fun remainder(nextIndex: Int) = this.copy(index = nextIndex)

    private fun <T> ok(match: T, index: Int, nextIndex: Int): Result<T> {
        return Result.Ok(match, index, remainder(nextIndex))
    }

    private fun <T> error(message: String, index: Int): Result<T> {
        return Result.Error(message, index, this)
    }

    private fun <T> fail(message: String, index: Int): Result<T> {
        return Result.Failure(message, index, this)
    }

    fun maybeSkipWhitespace(): Int {
        if (!skipWhitespace) return index

        var i = index
        while (i < input.length && input[i].let { it.isWhitespace() }) i++

        return i
    }

    fun take(literal: Char, ignoreCase: Boolean = false): Result<Char> {
        val index = maybeSkipWhitespace()

        if (index >= input.length) {
            return fail("Expected: '$literal', got unexpected end of input", index)
        } else if (input[index] != literal) {
            return fail("Expected: '$literal', got: '${input[index]}'", index)
        } else {
            return ok(literal, index, index + 1)
        }
    }

    fun take(literal: CharSequence, ignoreCase: Boolean = false): Result<CharSequence> {
        val index = maybeSkipWhitespace()

        if (input.startsWith(literal, startIndex = index, ignoreCase = ignoreCase)) {
            return ok(literal, index, index + literal.length)
        } else {
            return fail("Expected: '$literal'", index)
        }
    }
}