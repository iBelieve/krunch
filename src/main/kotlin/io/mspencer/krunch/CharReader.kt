package io.mspencer.krunch

data class CharReader(val source: CharSequence, override val index: Int = 0) : Reader<CharReader, Char> {
    override val atEnd get() = index >= source.length
    override val first get() = source[index]

    val position get() = Position.fromIndex(index, source)

    override fun remainder(nextIndex: Int) = CharReader(source, nextIndex)

    fun skipWhitespace(onlyCurrentLine: Boolean = false): CharReader {
        var nextIndex = index
        val shouldSkip = { it: Int ->
            if (onlyCurrentLine && nextIndex > 0 && source[it - 1] == '\n') {
                false
            } else {
                source[it].isWhitespace()
            }
        }

        while (nextIndex < source.length && shouldSkip(nextIndex)) nextIndex++

        return remainder(nextIndex)
    }

    fun take(literal: String, ignoreCase: Boolean = false): Result<CharReader, String> {
        if (source.startsWith(literal, startIndex = index, ignoreCase = ignoreCase)) {
            return ok(literal, index + literal.length)
        } else {
            return fail("Expected: '$literal'")
        }
    }

    fun take(regex: Regex): Result<CharReader, MatchResult> {
        val match = regex.find(source, startIndex = index)

        if (match == null || match.range.first != index) {
            return fail("Expected '$regex'")
        } else {
            return ok(match, match.range.endInclusive + 1)
        }
    }

    fun take(chars: CharArray): Result<CharReader, Char> {
        return next().let {
            when (it) {
                is Result.Ok -> when {
                    chars.contains(it.matched) -> it
                    else -> fail("Expected one of [${chars.map { "'$it'" }.joinToString(", ")}], got: '${it.matched}")
                }
                else -> it
            }
        }
    }
}