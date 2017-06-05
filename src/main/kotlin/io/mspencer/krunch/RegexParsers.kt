package io.mspencer.krunch

abstract class RegexParsers<out T> : CharParsers() {
    abstract val goal: Parser<CharReader, T>

    val endOfLine = literal("(\n|$)".toRegex()) map { Unit }

    val restOfLine = match("(.*?)(\n|$)".toRegex()).map { it.groupValues[1] }

    fun parse(source: String) = parse(goal, source)
}