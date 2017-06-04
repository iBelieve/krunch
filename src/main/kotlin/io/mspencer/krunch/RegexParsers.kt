package io.mspencer.krunch

abstract class RegexParsers<T> : CharParsers() {
    abstract val goal: Parser<CharReader, T>

    fun parse(source: String) = parse(goal, source)
}